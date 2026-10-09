package com.example

import com.example.data.linguistics.LinguisticAnalyzer
import com.example.data.model.SentenceClassification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LinguisticAnalyzerTest {

    @Test
    fun testWordAndSentenceSplitting() {
        val sample = "Hello world! This is a test. Are you ready?"
        val words = LinguisticAnalyzer.splitWords(sample)
        val sentences = LinguisticAnalyzer.splitSentences(sample)

        assertEquals(9, words.size)
        assertEquals(3, sentences.size)
        assertEquals("Hello world!", sentences[0])
        assertEquals("This is a test.", sentences[1])
        assertEquals("Are you ready?", sentences[2])
    }

    @Test
    fun testTextStatisticsCalculation() {
        val paragraphText = "First sentence is here.\n\nSecond paragraph begins with another sentence."
        val stats = LinguisticAnalyzer.calculateStatistics(paragraphText)

        assertTrue(stats.wordCount > 5)
        assertEquals(2, stats.sentenceCount)
        assertEquals(2, stats.paragraphCount)
        assertTrue(stats.lexicalDiversity > 0.0)
    }

    @Test
    fun testShortTextHandling() {
        val short = "Hi there"
        val result = LinguisticAnalyzer.analyzeHeuristic(short)

        assertEquals("Low", result.confidence)
        assertEquals(50, result.aiPercentage)
        assertEquals(50, result.humanPercentage)
        assertEquals("Text Too Short", result.overallAssessment)
    }

    @Test
    fun testAiTropeDetection() {
        val aiTropeText = "In today's digital landscape, artificial intelligence plays a crucial role. Furthermore, it delves into the rich tapestry of human history to foster innovation. Moreover, it is a testament to technological progress."
        val result = LinguisticAnalyzer.analyzeHeuristic(aiTropeText)

        assertTrue("AI percentage should reflect detected markers", result.aiPercentage > 50)
        assertTrue("Human percentage must complement AI percentage", result.aiPercentage + result.humanPercentage == 100)
        assertNotNull(result.detailedExplanation)
        assertTrue(result.sentenceAnalyses.isNotEmpty())

        val hasAiClassifiedSentence = result.sentenceAnalyses.any { it.classification == SentenceClassification.AI_LIKELY }
        assertTrue("Should classify at least one sentence as AI likely", hasAiClassifiedSentence)
    }

    @Test
    fun testHumanCadenceDetection() {
        val humanText = "Wow! I never thought we'd actually make it to the trailhead before dark. After climbing over three fallen logs and getting soaked by an unexpected afternoon thunderstorm, we finally reached the cabin and started a warm campfire. Everyone threw their damp boots by the hearth, laughed about the crazy hike, and drank hot cider."
        val result = LinguisticAnalyzer.analyzeHeuristic(humanText)

        assertTrue("Human percentage should be relatively strong", result.humanPercentage >= 40)
        assertTrue("Confidence should be Medium or High for sufficient length", result.confidence != "Low")
        assertTrue(result.statistics.burstinessScore > 0.0)
    }
}
