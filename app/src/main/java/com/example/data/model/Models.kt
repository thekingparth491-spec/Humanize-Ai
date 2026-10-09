package com.example.data.model

enum class SentenceClassification {
    AI_LIKELY,
    MIXED,
    HUMAN_LIKELY
}

data class SentenceScore(
    val sentence: String,
    val aiScore: Int,
    val classification: SentenceClassification,
    val reasoning: String
)

data class TextStatistics(
    val wordCount: Int,
    val charCount: Int,
    val sentenceCount: Int,
    val paragraphCount: Int,
    val avgSentenceLength: Double,
    val lexicalDiversity: Double, // Type-token ratio 0.0 - 1.0
    val burstinessScore: Double, // Variance in sentence lengths
    val readingTimeSeconds: Int
)

data class TextAnalysisResult(
    val aiPercentage: Int,
    val humanPercentage: Int,
    val confidence: String, // "High", "Medium", "Low"
    val overallAssessment: String,
    val detailedExplanation: String,
    val sentenceAnalyses: List<SentenceScore>,
    val statistics: TextStatistics,
    val detectionMethod: String,
    val reliabilityLimitation: String
)

enum class HumanizeStyle(val displayName: String, val description: String) {
    NATURAL_CONVERSATIONAL("Natural & Conversational", "Warm, authentic flow with varied phrasing and human rhythm"),
    SIMPLE_ENGLISH("Simple English", "Clear, direct words that are easy to understand"),
    STUDENT_FRIENDLY("Student-Friendly", "Relatable tone perfect for essays and coursework"),
    PROFESSIONAL_NATURAL("Professional but Natural", "Polished and executive without sounding robotic"),
    FRIENDLY_CASUAL("Friendly & Casual", "Breezy and approachable with natural idioms"),
    ACADEMIC_READABLE("Academic & Readable", "Scholarly depth with clear syntax and no stiff jargon"),
    HINDI("Hindi (हिन्दी)", "Pure, natural Hindi translation and phrasing"),
    HINGLISH("Hinglish", "Conversational urban blend of Hindi and English"),
    CUSTOM("Custom Instructions", "Rewrite following your specific stylistic guidance")
}

data class HumanizeOptions(
    val style: HumanizeStyle = HumanizeStyle.NATURAL_CONVERSATIONAL,
    val tone: String = "Balanced", // Warm, Casual, Professional, Direct
    val readingDifficulty: String = "Standard", // Easy, Standard, Advanced
    val sentenceLength: String = "Varied", // Short, Varied, Flowing
    val outputLength: String = "Similar", // Concise, Similar, Expanded
    val vocabularySimplicity: String = "Natural", // Simple, Natural, Rich
    val customInstructions: String = ""
)

data class HumanizeResult(
    val originalText: String,
    val humanizedText: String,
    val options: HumanizeOptions,
    val wordCountBefore: Int,
    val wordCountAfter: Int,
    val changesSummary: String
)

enum class AiStyleType(val displayName: String, val description: String) {
    FORMAL_ASSISTANT("Formal AI Assistant", "Structured, precise, polite AI-agent tone"),
    STRUCTURED_PROFESSIONAL("Structured Professional", "Executive summary with bulleted synthesis"),
    TECHNICAL_EXPLANATORY("Technical Explanatory", "Deep clarity with precise architectural vocabulary"),
    ACADEMIC_PROSE("Academic-Style Prose", "Formal discourse with rigorous analytical framing"),
    CONCISE_INFORMATIONAL("Concise Informational", "High density, zero fluff, direct answers"),
    DETAILED_STRUCTURED("Detailed Structured AI", "Comprehensive hierarchy with headings and logical progression")
}

data class AiStyleOptions(
    val style: AiStyleType = AiStyleType.STRUCTURED_PROFESSIONAL,
    val formalityLevel: String = "High", // Moderate, High, Maximum
    val detailLevel: String = "Detailed", // Concise, Balanced, In-depth
    val vocabularyLevel: String = "Technical", // Standard, Technical, Academic
    val outputLength: String = "Standard"
)

data class AiStyleResult(
    val originalText: String,
    val aiStyledText: String,
    val options: AiStyleOptions,
    val wordCountBefore: Int,
    val wordCountAfter: Int
)

enum class ImproveToolType(val displayName: String, val description: String) {
    GRAMMAR("Grammar Fix", "Correct grammatical mistakes and syntax errors"),
    SPELLING("Spelling Check", "Fix typographical and spelling mistakes"),
    REPHRASE("Rephrase & Enhance", "Enhance flow, tone, and vocabulary choice"),
    SUMMARIZE("Summarize", "Distill key takeaways into a concise summary"),
    EXPAND("Expand Text", "Elaborate with constructive depth and supporting context"),
    TRANSLATE("Translate", "Translate into Spanish, French, German, Hindi, Japanese, etc."),
    RESTRUCTURE("Restructure", "Reorganize paragraphs for maximum logical impact")
}

data class ImproveResult(
    val originalText: String,
    val improvedText: String,
    val toolType: ImproveToolType,
    val targetLanguage: String = "English",
    val notes: String = ""
)
