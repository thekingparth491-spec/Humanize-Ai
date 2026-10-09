package com.example.ui.checker

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.PrimaryGradient
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AiCheckerScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit
) {
    val context = LocalContext.current
    val inputText by viewModel.checkerInput.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()
    val errorMessage by viewModel.checkerError.collectAsState()
    val wordCount = viewModel.getWordCount(inputText)

    LaunchedEffect(analysisResult) {
        if (analysisResult != null && !isAnalyzing) {
            onNavigateToResult()
        }
    }

    val samplePresets = listOf(
        "AI Trope Sample" to "In today's fast-paced digital landscape, artificial intelligence plays a crucial role. Furthermore, it delves into the rich tapestry of human knowledge to foster innovation. Moreover, this technology serves as a testament to progress.",
        "Human Voice Sample" to "I honestly didn't expect the hike to be so brutal. My boots were completely caked in mud after five minutes! Still, reaching the summit ridge made every scrape worth it.",
        "Formal Tech Sample" to "The distributed cache invalidation protocol relies on quorum consensus across three availability zones. Upon socket timeout, the secondary replica attempts reconnection within a 200 millisecond exponential window."
    )

    Scaffold(
        topBar = {
            AppHeader(
                title = "AI Text Checker",
                onBackClick = onNavigateBack
            )
        },
        containerColor = NavyBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("ai_checker_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NavyBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Paste or Type Text",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Row {
                            TextButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = clipboard.primaryClip
                                    if (clip != null && clip.itemCount > 0) {
                                        val text = clip.getItemAt(0).text?.toString() ?: ""
                                        viewModel.setCheckerInput(text)
                                        Toast.makeText(context, "Pasted text", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.testTag("checker_paste_button")
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Paste", color = CyanAccent, fontSize = 12.sp)
                            }

                            if (inputText.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.clearCheckerInput() },
                                    modifier = Modifier.size(36.dp).testTag("checker_clear_button")
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.setCheckerInput(it) },
                        placeholder = {
                            Text(
                                "Enter at least a sentence or paragraph to check for AI-like uniformity, perplexity, and stylometric patterns...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .testTag("checker_text_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = VioletPrimary,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$wordCount words | ${inputText.length} characters",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (wordCount in 1..25) {
                            Text(
                                text = "Short text (heuristic mode)",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent
                            )
                        }
                    }
                }
            }

            // Quick Samples Row
            Column {
                Text(
                    text = "Quick Test Samples:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(samplePresets) { (label, sample) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .clickable { viewModel.setCheckerInput(sample) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(label, fontSize = 12.sp, color = CyanAccent)
                        }
                    }
                }
            }

            // Error Notice
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.4f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFCA5A5))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }
            }

            // Analysis Methodology Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = VioletPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Analyzes measurable linguistic features: sentence length burstiness, vocabulary entropy, and transition uniformity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Action Button
            Button(
                onClick = { viewModel.analyzeText() },
                enabled = !isAnalyzing && inputText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryGradient)
                    .testTag("checker_analyze_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Analyzing Text...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analyze Text",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
