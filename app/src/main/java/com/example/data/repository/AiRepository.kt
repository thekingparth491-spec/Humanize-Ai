package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.AnalyzeRequest
import com.example.data.api.BackendApiService
import com.example.data.api.HumanizeRequest
import com.example.data.api.AiStyleRequest
import com.example.data.api.ImproveRequest
import com.example.data.api.GeminiClient
import com.example.data.database.HistoryDao
import com.example.data.database.HistoryEntity
import com.example.data.linguistics.LinguisticAnalyzer
import com.example.data.model.AiStyleOptions
import com.example.data.model.AiStyleResult
import com.example.data.model.HumanizeOptions
import com.example.data.model.HumanizeResult
import com.example.data.model.ImproveResult
import com.example.data.model.ImproveToolType
import com.example.data.model.SentenceClassification
import com.example.data.model.SentenceScore
import com.example.data.model.TextAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AiRepository(
    private val historyDao: HistoryDao,
    private val settingsRepository: SettingsRepository
) {
    private val geminiClient = GeminiClient(
        defaultApiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }
    )

    private fun getEffectiveApiKey(): String {
        val customKey = settingsRepository.settings.value.customGeminiKey
        if (customKey.isNotBlank()) return customKey
        return try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }
    }

    suspend fun analyzeText(text: String): Result<TextAnalysisResult> = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Text cannot be empty."))
        }

        val settings = settingsRepository.settings.value
        if (trimmed.length > settings.textLimit) {
            return@withContext Result.failure(
                IllegalArgumentException("Text exceeds maximum character limit (${settings.textLimit} characters).")
            )
        }

        // 1. If backend URL is specified, try REST backend
        if (settings.backendUrl.isNotBlank()) {
            try {
                val api = BackendApiService.create(settings.backendUrl)
                val response = api.analyzeText(AnalyzeRequest(text = trimmed))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val stats = LinguisticAnalyzer.calculateStatistics(trimmed)
                    val sentences = body.sentenceAnalyses.map { dto ->
                        val classification = when (dto.classification.uppercase()) {
                            "AI_LIKELY" -> SentenceClassification.AI_LIKELY
                            "HUMAN_LIKELY" -> SentenceClassification.HUMAN_LIKELY
                            else -> SentenceClassification.MIXED
                        }
                        SentenceScore(
                            sentence = dto.sentence,
                            aiScore = dto.aiScore,
                            classification = classification,
                            reasoning = dto.reasoning
                        )
                    }

                    val result = TextAnalysisResult(
                        aiPercentage = body.aiPercentage,
                        humanPercentage = body.humanPercentage,
                        confidence = body.confidence,
                        overallAssessment = body.overallAssessment,
                        detailedExplanation = body.detailedExplanation,
                        sentenceAnalyses = if (sentences.isNotEmpty()) sentences else LinguisticAnalyzer.analyzeHeuristic(trimmed).sentenceAnalyses,
                        statistics = stats,
                        detectionMethod = body.detectionMethod,
                        reliabilityLimitation = body.reliabilityLimitation.ifEmpty {
                            "AI text detectors can produce false positives and false negatives, particularly for short, edited, multilingual, and non-native English text. This result is an estimate."
                        }
                    )
                    saveHistoryIfEnabled("CHECKER", "AI Analysis: ${result.aiPercentage}% AI", trimmed, result.overallAssessment, result.aiPercentage, result.humanPercentage, stats.wordCount)
                    return@withContext Result.success(result)
                }
            } catch (e: Exception) {
                // If backend was explicitly specified but failed, fall back gracefully to heuristic/gemini with note
            }
        }

        // 2. Direct Gemini Linguistic Analysis (if key configured)
        val apiKey = getEffectiveApiKey()
        if (apiKey.isNotBlank() && !apiKey.contains("MY_GEMINI_API_KEY") && settings.useDirectGemini) {
            val stats = LinguisticAnalyzer.calculateStatistics(trimmed)
            val heuristic = LinguisticAnalyzer.analyzeHeuristic(trimmed)

            val prompt = """
                You are an objective linguistic auditor analyzing text for potential AI-generated vs human-written traits.
                Analyze the following text strictly and objectively based on burstiness (sentence length variance), perplexity (unusual vs predictable phrasing), lexical variety, and formulaic transitions.
                
                Text to analyze:
                \"\"\"$trimmed\"\"\"
                
                Measured computational metrics:
                - Word count: ${stats.wordCount}
                - Average sentence length: ${stats.avgSentenceLength} words
                - Burstiness std deviation: ${stats.burstinessScore}
                - Lexical diversity (TTR): ${stats.lexicalDiversity}
                
                Return a JSON object strictly conforming to this schema (do not wrap in markdown or backticks):
                {
                   "ai_percentage": <integer 0 to 100>,
                   "human_percentage": <integer 0 to 100>,
                   "confidence": "<High, Medium, or Low>",
                   "overall_assessment": "<Short title, e.g. High Likelihood of AI Generation, Mixed Influence, Likely Human>",
                   "detailed_explanation": "<Detailed factual explanation describing observed linguistic features, burstiness, and lexical traits>"
                }
            """.trimIndent()

            val aiResult = geminiClient.generateContent(
                prompt = prompt,
                systemInstruction = "You are a scientific text analyst. Analyze measurable linguistic indicators objectively without assuming all formal text is AI-generated.",
                apiKeyOverride = apiKey
            )

            if (aiResult.isSuccess) {
                try {
                    val rawJson = aiResult.getOrNull()!!.replace(Regex("^```json\\s*"), "").replace(Regex("```$"), "").trim()
                    val jsonObj = JSONObject(rawJson)
                    val aiScore = jsonObj.getInt("ai_percentage").coerceIn(5, 95)
                    val humanScore = jsonObj.getInt("human_percentage").coerceIn(5, 95)
                    val confidence = jsonObj.optString("confidence", heuristic.confidence)
                    val assessment = jsonObj.optString("overall_assessment", heuristic.overallAssessment)
                    val explanation = jsonObj.optString("detailed_explanation", heuristic.detailedExplanation)

                    val result = TextAnalysisResult(
                        aiPercentage = aiScore,
                        humanPercentage = humanScore,
                        confidence = confidence,
                        overallAssessment = assessment,
                        detailedExplanation = explanation,
                        sentenceAnalyses = heuristic.sentenceAnalyses,
                        statistics = stats,
                        detectionMethod = "Gemini Neural Classifier + Stylometric Engine",
                        reliabilityLimitation = "AI text detectors can produce false positives and false negatives, particularly for short, edited, multilingual, and non-native English text. This result is an estimate."
                    )
                    saveHistoryIfEnabled("CHECKER", "AI Analysis: $aiScore% AI", trimmed, assessment, aiScore, humanScore, stats.wordCount)
                    return@withContext Result.success(result)
                } catch (e: Exception) {
                    // Fall back to heuristic if JSON parsing fails
                }
            }
        }

        // 3. Built-in Heuristic & Linguistic Engine (always works, even offline or without keys)
        val heuristicResult = LinguisticAnalyzer.analyzeHeuristic(trimmed)
        saveHistoryIfEnabled(
            toolType = "CHECKER",
            title = "Linguistic Analysis: ${heuristicResult.aiPercentage}% AI",
            inputText = trimmed,
            outputText = heuristicResult.overallAssessment,
            aiScore = heuristicResult.aiPercentage,
            humanScore = heuristicResult.humanPercentage,
            wordCount = heuristicResult.statistics.wordCount
        )
        Result.success(heuristicResult)
    }

    suspend fun humanizeText(
        text: String,
        options: HumanizeOptions
    ): Result<HumanizeResult> = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter or paste text to humanize."))
        }

        val settings = settingsRepository.settings.value
        val wordCountBefore = LinguisticAnalyzer.splitWords(trimmed).size

        // Backend check
        if (settings.backendUrl.isNotBlank()) {
            try {
                val api = BackendApiService.create(settings.backendUrl)
                val response = api.humanizeText(
                    HumanizeRequest(
                        text = trimmed,
                        style = options.style.name,
                        tone = options.tone,
                        readingDifficulty = options.readingDifficulty,
                        sentenceLength = options.sentenceLength,
                        outputLength = options.outputLength,
                        vocabularySimplicity = options.vocabularySimplicity,
                        customInstructions = options.customInstructions
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val result = HumanizeResult(
                        originalText = trimmed,
                        humanizedText = body.humanizedText,
                        options = options,
                        wordCountBefore = body.wordCountBefore,
                        wordCountAfter = body.wordCountAfter,
                        changesSummary = body.changesSummary
                    )
                    saveHistoryIfEnabled("HUMANIZE", "Humanized (${options.style.displayName})", trimmed, result.humanizedText, wordCount = result.wordCountAfter)
                    return@withContext Result.success(result)
                }
            } catch (e: Exception) {
                // fallback to direct AI
            }
        }

        // Direct AI via Gemini
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank() || apiKey.contains("MY_GEMINI_API_KEY")) {
            return@withContext Result.failure(
                IllegalStateException("AI rewriting requires an active API key or connected backend. Please configure GEMINI_API_KEY in the Secrets panel or enter an API key in Settings.")
            )
        }

        val systemPrompt = """
            You are an expert human prose rewriter. Your purpose is to rewrite text to sound natural, engaging, and genuinely human.
            
            Strict Guidelines:
            1. Preserve ALL core facts, data, statistics, names, dates, technical terms, and intended meaning.
            2. Vary sentence structures naturally (create organic burstiness with a rhythm of punchy and expressive sentences).
            3. Eliminate robotic LLM clichés (e.g. "delve into", "rich tapestry", "a testament to", "in today's digital landscape", "crucial role", "furthermore").
            4. Never invent unsupported facts or citations.
            5. Do NOT claim this text is guaranteed to bypass AI detectors.
            6. Return ONLY the rewritten text without preambles, meta-commentary, or quotation marks.
        """.trimIndent()

        val styleGuidance = when (options.style) {
            com.example.data.model.HumanizeStyle.NATURAL_CONVERSATIONAL -> "Rewrite with a natural, conversational cadence and authentic human warmth."
            com.example.data.model.HumanizeStyle.SIMPLE_ENGLISH -> "Rewrite using simple, clear English vocabulary and direct, easy-to-read sentences."
            com.example.data.model.HumanizeStyle.STUDENT_FRIENDLY -> "Rewrite in an articulate, student-friendly tone suitable for coursework, essays, and assignments."
            com.example.data.model.HumanizeStyle.PROFESSIONAL_NATURAL -> "Rewrite in a natural executive tone that sounds like an experienced professional, not a sterile bot."
            com.example.data.model.HumanizeStyle.FRIENDLY_CASUAL -> "Rewrite in a relaxed, friendly, and approachable casual voice with natural idioms."
            com.example.data.model.HumanizeStyle.ACADEMIC_READABLE -> "Rewrite with academic depth and scholarly precision, but with clear syntax and zero fluff."
            com.example.data.model.HumanizeStyle.HINDI -> "Translate and humanize into pure, natural Hindi (Devanagari script) with authentic cultural idioms."
            com.example.data.model.HumanizeStyle.HINGLISH -> "Rewrite in authentic conversational Hinglish (blend of Hindi and English in Roman script) as naturally spoken in India."
            com.example.data.model.HumanizeStyle.CUSTOM -> "Rewrite strictly following these custom instructions: ${options.customInstructions}"
        }

        val prompt = """
            Style: ${options.style.displayName}
            Tone: ${options.tone}
            Reading Difficulty: ${options.readingDifficulty}
            Sentence Length Cadence: ${options.sentenceLength}
            Vocabulary: ${options.vocabularySimplicity}
            Guidance: $styleGuidance
            
            Original Text to Humanize:
            \"\"\"$trimmed\"\"\"
        """.trimIndent()

        val aiResponse = geminiClient.generateContent(
            prompt = prompt,
            systemInstruction = systemPrompt,
            apiKeyOverride = apiKey
        )

        aiResponse.fold(
            onSuccess = { rewrittenText ->
                val wordsAfter = LinguisticAnalyzer.splitWords(rewrittenText).size
                val summary = "Rewritten in ${options.style.displayName} with ${options.tone} tone and natural sentence cadence."
                val result = HumanizeResult(
                    originalText = trimmed,
                    humanizedText = rewrittenText,
                    options = options,
                    wordCountBefore = wordCountBefore,
                    wordCountAfter = wordsAfter,
                    changesSummary = summary
                )
                saveHistoryIfEnabled("HUMANIZE", "Humanized (${options.style.displayName})", trimmed, rewrittenText, wordCount = wordsAfter)
                Result.success(result)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    suspend fun convertAiStyle(
        text: String,
        options: AiStyleOptions
    ): Result<AiStyleResult> = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter or paste text to convert."))
        }

        val settings = settingsRepository.settings.value
        val wordCountBefore = LinguisticAnalyzer.splitWords(trimmed).size

        // Backend check
        if (settings.backendUrl.isNotBlank()) {
            try {
                val api = BackendApiService.create(settings.backendUrl)
                val response = api.convertAiStyle(
                    AiStyleRequest(
                        text = trimmed,
                        style = options.style.name,
                        formality = options.formalityLevel,
                        detail = options.detailLevel,
                        vocabulary = options.vocabularyLevel,
                        outputLength = options.outputLength
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val result = AiStyleResult(
                        originalText = trimmed,
                        aiStyledText = body.aiStyledText,
                        options = options,
                        wordCountBefore = body.wordCountBefore,
                        wordCountAfter = body.wordCountAfter
                    )
                    saveHistoryIfEnabled("AI_STYLE", "AI Style (${options.style.displayName})", trimmed, result.aiStyledText, wordCount = result.wordCountAfter)
                    return@withContext Result.success(result)
                }
            } catch (e: Exception) {
                // fallback to direct AI
            }
        }

        // Direct AI via Gemini
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank() || apiKey.contains("MY_GEMINI_API_KEY")) {
            return@withContext Result.failure(
                IllegalStateException("AI Style conversion requires an active API key or connected backend. Please configure GEMINI_API_KEY in the Secrets panel or Settings.")
            )
        }

        val styleInstruction = when (options.style) {
            com.example.data.model.AiStyleType.FORMAL_ASSISTANT -> "Transform into formal AI-assistant prose: structured, polite, analytically precise, objective."
            com.example.data.model.AiStyleType.STRUCTURED_PROFESSIONAL -> "Transform into structured professional output: executive overview, bulleted breakdown, actionable synthesis."
            com.example.data.model.AiStyleType.TECHNICAL_EXPLANATORY -> "Transform into technical explanatory style: architectural depth, rigorous definitions, logical flow."
            com.example.data.model.AiStyleType.ACADEMIC_PROSE -> "Transform into scholarly academic prose: formal discourse, analytical rigor, evidence-oriented framework."
            com.example.data.model.AiStyleType.CONCISE_INFORMATIONAL -> "Transform into concise informational style: zero filler, maximum density, clear bulleted points."
            com.example.data.model.AiStyleType.DETAILED_STRUCTURED -> "Transform into detailed, structured AI-generated-style writing with clear hierarchies and numbered headings."
        }

        val systemPrompt = """
            You are an AI Style transformation engine. Transform human writing into structured, clear, and cohesive AI-assisted formats.
            Preserve all underlying facts and core ideas while restructuring for maximum clarity and organization.
            Return ONLY the transformed text.
        """.trimIndent()

        val prompt = """
            Target AI Style: ${options.style.displayName}
            Formality: ${options.formalityLevel}
            Detail Level: ${options.detailLevel}
            Vocabulary: ${options.vocabularyLevel}
            Instructions: $styleInstruction
            
            Text to convert:
            \"\"\"$trimmed\"\"\"
        """.trimIndent()

        val aiResponse = geminiClient.generateContent(
            prompt = prompt,
            systemInstruction = systemPrompt,
            apiKeyOverride = apiKey
        )

        aiResponse.fold(
            onSuccess = { styledText ->
                val wordsAfter = LinguisticAnalyzer.splitWords(styledText).size
                val result = AiStyleResult(
                    originalText = trimmed,
                    aiStyledText = styledText,
                    options = options,
                    wordCountBefore = wordCountBefore,
                    wordCountAfter = wordsAfter
                )
                saveHistoryIfEnabled("AI_STYLE", "AI Style (${options.style.displayName})", trimmed, styledText, wordCount = wordsAfter)
                Result.success(result)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    suspend fun improveText(
        text: String,
        toolType: ImproveToolType,
        targetLanguage: String = "English"
    ): Result<ImproveResult> = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter text to improve."))
        }

        val settings = settingsRepository.settings.value

        // Backend check
        if (settings.backendUrl.isNotBlank()) {
            try {
                val api = BackendApiService.create(settings.backendUrl)
                val response = api.improveText(
                    ImproveRequest(
                        text = trimmed,
                        toolType = toolType.name,
                        targetLanguage = targetLanguage
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val result = ImproveResult(
                        originalText = trimmed,
                        improvedText = body.improvedText,
                        toolType = toolType,
                        targetLanguage = targetLanguage,
                        notes = body.notes
                    )
                    saveHistoryIfEnabled("IMPROVE", "Improved (${toolType.displayName})", trimmed, result.improvedText, wordCount = LinguisticAnalyzer.splitWords(result.improvedText).size)
                    return@withContext Result.success(result)
                }
            } catch (e: Exception) {
                // fallback
            }
        }

        // Direct AI
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank() || apiKey.contains("MY_GEMINI_API_KEY")) {
            return@withContext Result.failure(
                IllegalStateException("Text improvement requires an active API key or connected backend. Please configure GEMINI_API_KEY in the Secrets panel or Settings.")
            )
        }

        val taskPrompt = when (toolType) {
            ImproveToolType.GRAMMAR -> "Correct all grammatical errors, subject-verb disagreements, and punctuation issues. Keep the original wording as close as possible."
            ImproveToolType.SPELLING -> "Detect and correct all spelling and typographical errors while keeping words and sentence structure intact."
            ImproveToolType.REPHRASE -> "Rephrase sentences for optimal rhythm, clearer expression, and natural eloquence without changing the original meaning."
            ImproveToolType.SUMMARIZE -> "Summarize the key ideas, findings, and arguments into a concise, well-structured executive summary."
            ImproveToolType.EXPAND -> "Expand the provided text with helpful context, thorough explanations, and illustrative detail while respecting the core premise."
            ImproveToolType.TRANSLATE -> "Translate the text accurately and naturally into $targetLanguage, preserving nuance, idioms, and tone."
            ImproveToolType.RESTRUCTURE -> "Reorganize the paragraphs and flow for maximum logical coherence, transitions, and clarity."
        }

        val systemPrompt = """
            You are a master professional text editor and linguist.
            Execute the requested text improvement task cleanly.
            Return ONLY the improved result without preambles or explanations.
        """.trimIndent()

        val prompt = """
            Task: $taskPrompt
            Target Language: $targetLanguage
            
            Text:
            \"\"\"$trimmed\"\"\"
        """.trimIndent()

        val aiResponse = geminiClient.generateContent(
            prompt = prompt,
            systemInstruction = systemPrompt,
            apiKeyOverride = apiKey
        )

        aiResponse.fold(
            onSuccess = { improvedText ->
                val result = ImproveResult(
                    originalText = trimmed,
                    improvedText = improvedText,
                    toolType = toolType,
                    targetLanguage = targetLanguage,
                    notes = "Processed with ${toolType.displayName}"
                )
                saveHistoryIfEnabled("IMPROVE", "Improved (${toolType.displayName})", trimmed, improvedText, wordCount = LinguisticAnalyzer.splitWords(improvedText).size)
                Result.success(result)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    suspend fun testBackendHealth(url: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val api = BackendApiService.create(url)
            val response = api.checkHealth()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success("Connected! Server status: ${body.status}, version: ${body.version}, provider: ${body.provider}")
            } else {
                Result.failure(Exception("Server returned HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to connect: ${e.localizedMessage ?: "Connection refused"}"))
        }
    }

    private suspend fun saveHistoryIfEnabled(
        toolType: String,
        title: String,
        inputText: String,
        outputText: String,
        aiScore: Int = 0,
        humanScore: Int = 0,
        wordCount: Int = 0,
        style: String = ""
    ) {
        if (!settingsRepository.settings.value.historyEnabled) return
        try {
            historyDao.insert(
                HistoryEntity(
                    toolType = toolType,
                    title = title,
                    inputText = inputText,
                    outputText = outputText,
                    styleSelected = style,
                    aiScore = aiScore,
                    humanScore = humanScore,
                    wordCount = wordCount
                )
            )
        } catch (e: Exception) {
            // Ignore database write failures gracefully
        }
    }
}
