package com.example.data.linguistics

import com.example.data.model.SentenceClassification
import com.example.data.model.SentenceScore
import com.example.data.model.TextAnalysisResult
import com.example.data.model.TextStatistics
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object LinguisticAnalyzer {

    // Common formulaic phrases and lexical markers frequently over-indexed in typical LLM outputs
    private val AI_FORMULAIC_MARKERS = listOf(
        "delve", "delves", "delving",
        "tapestry", "rich tapestry",
        "testament to", "a testament",
        "crucial", "crucial role",
        "pivotal", "pivotal role",
        "moreover", "furthermore",
        "in conclusion", "to conclude",
        "it is important to note", "it is worth noting",
        "foster", "fostering",
        "unlock", "unlocking",
        "harness", "harnessing",
        "navigating the", "navigating",
        "beacon", "beacon of",
        "game-changer", "game changer",
        "in today's fast-paced", "in today's digital landscape",
        "serves as a", "underscores the",
        "multifaceted", "plethora",
        "seamlessly", "dive into", "dive deeper"
    )

    fun splitSentences(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        val regex = Regex("(?<=[.!?])\\s+(?=[A-Z0-9\"'\\p{L}])|(?<=[.!?])\\n+")
        return text.split(regex)
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    fun splitWords(text: String): List<String> {
        val wordRegex = Regex("[\\p{L}0-9'-]+")
        return wordRegex.findAll(text)
            .map { it.value.lowercase(Locale.ROOT) }
            .filter { it.length > 1 || it == "a" || it == "i" }
            .toList()
    }

    fun calculateStatistics(text: String): TextStatistics {
        val words = splitWords(text)
        val sentences = splitSentences(text)
        val paragraphs = text.split(Regex("\\n\\s*\\n")).filter { it.isNotBlank() }

        val wordCount = words.size
        val charCount = text.length
        val sentenceCount = max(1, sentences.size)
        val paragraphCount = max(1, paragraphs.size)

        val avgSentenceLength = if (sentences.isNotEmpty()) {
            words.size.toDouble() / sentences.size
        } else 0.0

        // Type-Token Ratio (Lexical Diversity)
        val uniqueWords = words.toSet().size
        val lexicalDiversity = if (words.isNotEmpty()) {
            uniqueWords.toDouble() / words.size
        } else 0.0

        // Sentence Length Variance (Burstiness)
        val sentenceLengths = sentences.map { splitWords(it).size.toDouble() }
        val burstinessScore = if (sentenceLengths.size > 1) {
            val mean = sentenceLengths.average()
            val variance = sentenceLengths.map { (it - mean).pow(2) }.average()
            sqrt(variance)
        } else {
            0.0
        }

        val readingTimeSeconds = max(1, (wordCount / 3.5).roundToInt())

        return TextStatistics(
            wordCount = wordCount,
            charCount = charCount,
            sentenceCount = sentenceCount,
            paragraphCount = paragraphCount,
            avgSentenceLength = (avgSentenceLength * 10).roundToInt() / 10.0,
            lexicalDiversity = (lexicalDiversity * 100).roundToInt() / 100.0,
            burstinessScore = (burstinessScore * 10).roundToInt() / 10.0,
            readingTimeSeconds = readingTimeSeconds
        )
    }

    fun analyzeHeuristic(text: String): TextAnalysisResult {
        val stats = calculateStatistics(text)
        val sentences = splitSentences(text)
        val words = splitWords(text)
        val lowercaseText = text.lowercase(Locale.ROOT)

        if (stats.wordCount < 5) {
            return TextAnalysisResult(
                aiPercentage = 50,
                humanPercentage = 50,
                confidence = "Low",
                overallAssessment = "Text Too Short",
                detailedExplanation = "The input text contains fewer than 5 words. Linguistic variance, burstiness, and lexical entropy cannot be measured on very short fragments.",
                sentenceAnalyses = sentences.map {
                    SentenceScore(it, 50, SentenceClassification.MIXED, "Sample size too small for statistical analysis.")
                },
                statistics = stats,
                detectionMethod = "Heuristic Linguistic Engine (Stylometric & Burstiness)",
                reliabilityLimitation = "AI text detectors can produce false positives and false negatives, especially on short, edited, multilingual, or non-native English text. This result is an experimental estimate."
            )
        }

        // 1. Metric: Formulaic marker count
        var markerHits = 0
        val foundMarkers = mutableListOf<String>()
        for (marker in AI_FORMULAIC_MARKERS) {
            val count = Regex("\\b${Regex.escape(marker)}\\b").findAll(lowercaseText).count()
            if (count > 0) {
                markerHits += count
                foundMarkers.add(marker)
            }
        }
        val markerDensityPer100Words = if (stats.wordCount > 0) {
            (markerHits.toDouble() / stats.wordCount) * 100
        } else 0.0

        // 2. Metric: Sentence Length Uniformity (Burstiness)
        // High standard deviation indicates human burstiness (mix of short and long sentences).
        // Low standard deviation indicates uniform robotic pacing.
        val burstinessPenalty = when {
            stats.sentenceCount <= 2 -> 0.5 // neutral
            stats.burstinessScore < 2.5 -> 0.85 // highly uniform (AI-like)
            stats.burstinessScore < 4.5 -> 0.65 // moderately uniform
            stats.burstinessScore < 7.0 -> 0.40 // varied
            else -> 0.15 // highly varied, dynamic human cadence
        }

        // 3. Metric: Lexical Diversity (TTR)
        // Highly repetitive text or flat vocabulary:
        val lexicalFactor = when {
            stats.lexicalDiversity < 0.45 -> 0.75 // repetitive
            stats.lexicalDiversity < 0.65 -> 0.50
            else -> 0.25 // rich human vocabulary
        }

        // 4. Metric: AI Marker Factor
        val markerFactor = when {
            markerDensityPer100Words > 3.0 -> 0.90
            markerDensityPer100Words > 1.5 -> 0.70
            markerDensityPer100Words > 0.5 -> 0.55
            else -> 0.20
        }

        // Weighted raw AI likelihood (0.0 to 1.0)
        val rawAiScore = (burstinessPenalty * 0.40) + (markerFactor * 0.35) + (lexicalFactor * 0.25)
        var aiPercentage = (rawAiScore * 100).roundToInt().coerceIn(5, 95)
        var humanPercentage = 100 - aiPercentage

        // Sentence-level breakdown
        val sentenceScores = sentences.map { sentence ->
            val sWords = splitWords(sentence)
            val sWordCount = sWords.size
            val sLower = sentence.lowercase(Locale.ROOT)

            var sMarkerCount = 0
            val sFound = mutableListOf<String>()
            for (marker in AI_FORMULAIC_MARKERS) {
                if (Regex("\\b${Regex.escape(marker)}\\b").containsMatchIn(sLower)) {
                    sMarkerCount++
                    sFound.add(marker)
                }
            }

            // Deviation from average sentence length
            val diffFromMean = kotlin.math.abs(sWordCount - stats.avgSentenceLength)
            val isAverageLength = diffFromMean < 3.0

            var sentenceAi = when {
                sMarkerCount >= 2 -> 85
                sMarkerCount == 1 -> 65
                isAverageLength && stats.sentenceCount > 3 -> 55
                diffFromMean > 8.0 -> 25 // unusually short or long -> human burst
                else -> 45
            }
            // Blend with global score slightly for context
            sentenceAi = ((sentenceAi * 0.7) + (aiPercentage * 0.3)).roundToInt().coerceIn(5, 95)

            val classification = when {
                sentenceAi >= 65 -> SentenceClassification.AI_LIKELY
                sentenceAi <= 38 -> SentenceClassification.HUMAN_LIKELY
                else -> SentenceClassification.MIXED
            }

            val reasoning = when {
                sFound.isNotEmpty() -> "Contains conventional LLM markers (${sFound.take(2).joinToString(", ")}) and standard phrasing."
                classification == SentenceClassification.AI_LIKELY -> "High syntactical uniformity and predictable transition structure."
                classification == SentenceClassification.HUMAN_LIKELY -> "Natural length cadence ($sWordCount words) and distinctive lexical choices."
                else -> "Balanced phrasing with standard syntactic distribution."
            }

            SentenceScore(
                sentence = sentence,
                aiScore = sentenceAi,
                classification = classification,
                reasoning = reasoning
            )
        }

        // Confidence determination based on sample size
        val confidence = when {
            stats.wordCount < 40 -> "Low"
            stats.wordCount < 120 -> "Medium"
            else -> "High"
        }

        // Assessment text
        val overallAssessment = when {
            aiPercentage >= 70 -> "High Likelihood of AI Generation"
            aiPercentage >= 50 -> "Mixed / Moderately AI-Influenced"
            aiPercentage >= 30 -> "Likely Human with Formal Elements"
            else -> "Predominantly Human-Authored"
        }

        val explanationBuilder = StringBuilder()
        explanationBuilder.append("Analysis based on stylometric variance, sentence burstiness, and lexical distribution:\n")
        explanationBuilder.append("• Sentence Burstiness: ${stats.burstinessScore} standard deviation (${if (stats.burstinessScore < 4.0) "Uniform pacing typical of language models" else "Natural cadence variation common in human prose"}).\n")
        explanationBuilder.append("• Lexical Diversity: ${(stats.lexicalDiversity * 100).toInt()}% unique vocabulary ratio.\n")
        if (foundMarkers.isNotEmpty()) {
            explanationBuilder.append("• Formulaic Markers Detected: ${foundMarkers.distinct().take(4).joinToString(", ")}.\n")
        } else {
            explanationBuilder.append("• Formulaic Markers: No excessive LLM transition tropes identified.\n")
        }
        explanationBuilder.append("• Structural Pacing: Average sentence length is ${stats.avgSentenceLength} words across ${stats.sentenceCount} sentence(s).")

        return TextAnalysisResult(
            aiPercentage = aiPercentage,
            humanPercentage = humanPercentage,
            confidence = confidence,
            overallAssessment = overallAssessment,
            detailedExplanation = explanationBuilder.toString(),
            sentenceAnalyses = sentenceScores,
            statistics = stats,
            detectionMethod = "Heuristic Stylometric & Perplexity Engine",
            reliabilityLimitation = "AI text detectors can produce false positives and false negatives, particularly for short, edited, multilingual, and non-native English text. This result is an experimental estimate."
        )
    }
}
