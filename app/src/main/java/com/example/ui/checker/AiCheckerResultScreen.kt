package com.example.ui.checker

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppHeader
import com.example.ui.components.CircularScoreGauge
import com.example.ui.components.DualLikelihoodBar
import com.example.ui.components.GlassCard
import com.example.ui.components.SentenceHighlightCard
import com.example.ui.components.StatMetricBadge
import com.example.ui.theme.AiScoreOrange
import com.example.ui.theme.AiScoreRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.HumanScoreEmerald
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.PrimaryGradient
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AiCheckerResultScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val result by viewModel.analysisResult.collectAsState()
    var selectedSentenceIndex by remember { mutableIntStateOf(-1) }

    if (result == null) {
        onNavigateBack()
        return
    }

    val res = result!!

    fun copyReport() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val report = buildString {
            appendLine("=== HumanizeAI Analysis Report ===")
            appendLine("AI-like writing: ${res.aiPercentage}%")
            appendLine("Human-like writing: ${res.humanPercentage}%")
            appendLine("Assessment: ${res.overallAssessment}")
            appendLine("Confidence: ${res.confidence}")
            appendLine("Method: ${res.detectionMethod}")
            appendLine()
            appendLine("Statistics:")
            appendLine("• Words: ${res.statistics.wordCount} | Characters: ${res.statistics.charCount}")
            appendLine("• Sentences: ${res.statistics.sentenceCount} | Paragraphs: ${res.statistics.paragraphCount}")
            appendLine("• Burstiness std dev: ${res.statistics.burstinessScore}")
            appendLine("• Lexical diversity: ${res.statistics.lexicalDiversity}")
            appendLine()
            appendLine("Explanation:")
            appendLine(res.detailedExplanation)
            appendLine()
            appendLine("Disclaimer:")
            appendLine(res.reliabilityLimitation)
        }
        val clip = ClipData.newPlainText("HumanizeAI Report", report)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Report copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareReport() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "HumanizeAI Text Analysis Result")
            putExtra(
                Intent.EXTRA_TEXT,
                "HumanizeAI Detection Report:\nAI-like: ${res.aiPercentage}%\nHuman-like: ${res.humanPercentage}%\nAssessment: ${res.overallAssessment}\nConfidence: ${res.confidence}\nMethod: ${res.detectionMethod}"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Analysis Result"))
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Analysis Results",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { copyReport() }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Report", tint = Color.White)
                    }
                    IconButton(onClick = { shareReport() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                }
            )
        },
        containerColor = NavyBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .testTag("ai_checker_result_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Gauge Card
            item {
                GlassCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Confidence Tag
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Confidence: ${res.confidence}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.confidence == "High") HumanScoreEmerald else CyanAccent
                                )
                            }

                            Text(
                                text = res.detectionMethod,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        CircularScoreGauge(
                            aiScore = res.aiPercentage,
                            humanScore = res.humanPercentage
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = res.overallAssessment,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (res.aiPercentage >= 50) AiScoreOrange else HumanScoreEmerald
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        DualLikelihoodBar(
                            aiScore = res.aiPercentage,
                            humanScore = res.humanPercentage
                        )
                    }
                }
            }

            // Stats Grid
            item {
                Text(
                    text = "Text Metrics & Pacing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricBadge(
                        icon = Icons.Default.Assessment,
                        label = "Total Words",
                        value = "${res.statistics.wordCount}",
                        modifier = Modifier.weight(1f),
                        accentColor = VioletPrimary
                    )
                    StatMetricBadge(
                        icon = Icons.Default.Title,
                        label = "Characters",
                        value = "${res.statistics.charCount}",
                        modifier = Modifier.weight(1f),
                        accentColor = CyanAccent
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricBadge(
                        icon = Icons.Default.FormatListNumbered,
                        label = "Sentences",
                        value = "${res.statistics.sentenceCount}",
                        modifier = Modifier.weight(1f),
                        accentColor = Color(0xFF3B82F6)
                    )
                    StatMetricBadge(
                        icon = Icons.Default.Timeline,
                        label = "Burstiness (SD)",
                        value = "${res.statistics.burstinessScore}",
                        modifier = Modifier.weight(1f),
                        accentColor = if (res.statistics.burstinessScore < 3.5) AiScoreRed else HumanScoreEmerald
                    )
                }
            }

            // Detailed Explanation Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NavyBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Linguistic Explanation",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = res.detailedExplanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Sentence Breakdown Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sentence-Level Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Tap sentence for details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            itemsIndexed(res.sentenceAnalyses) { index, sentenceScore ->
                SentenceHighlightCard(
                    sentenceScore = sentenceScore,
                    isSelected = selectedSentenceIndex == index,
                    onClick = {
                        selectedSentenceIndex = if (selectedSentenceIndex == index) -1 else index
                    }
                )
            }

            // Scientific Limitation Disclaimer Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E).copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Analysis Disclaimer",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = res.reliabilityLimitation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { copyReport() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("result_copy_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VioletPrimary)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = VioletPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Report", color = VioletPrimary)
                    }

                    Button(
                        onClick = {
                            viewModel.clearCheckerInput()
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("result_analyze_again_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analyze New", color = Color.White)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
