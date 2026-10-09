package com.example.ui.humanizer

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.model.HumanizeStyle
import com.example.ui.components.AppHeader
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.PrimaryGradient
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HumanizerScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit
) {
    val context = LocalContext.current
    val inputText by viewModel.humanizeInput.collectAsState()
    val options by viewModel.humanizeOptions.collectAsState()
    val isHumanizing by viewModel.isHumanizing.collectAsState()
    val humanizeResult by viewModel.humanizeResult.collectAsState()
    val errorMessage by viewModel.humanizeError.collectAsState()
    val wordCount = viewModel.getWordCount(inputText)
    var showFineTuning by remember { mutableStateOf(false) }

    LaunchedEffect(humanizeResult) {
        if (humanizeResult != null && !isHumanizing) {
            onNavigateToResult()
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Humanize Text",
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
                .testTag("humanizer_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Text Input Card
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
                            text = "AI / Formal Text to Humanize",
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
                                        viewModel.setHumanizeInput(text)
                                        Toast.makeText(context, "Pasted text", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Paste", color = CyanAccent, fontSize = 12.sp)
                            }
                            if (inputText.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.setHumanizeInput("") },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.setHumanizeInput(it) },
                        placeholder = {
                            Text(
                                "Paste AI-generated or rigid text here. We'll rewrite it with natural human rhythm, organic burstiness, and authentic cadence...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .testTag("humanize_text_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$wordCount words | ${inputText.length} chars",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Rewriting Style Section
            Column {
                Text(
                    text = "Select Rewriting Style",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HumanizeStyle.entries.forEach { style ->
                        val isSelected = options.style == style
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setHumanizeStyle(style) },
                            label = { Text(style.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent,
                                selectedLabelColor = NavyBackground,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) CyanAccent else NavyBorder
                            )
                        )
                    }
                }

                // Active style description
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = options.style.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanAccentLight(),
                    lineHeight = 16.sp
                )
            }

            // Custom Instructions Field
            if (options.style == HumanizeStyle.CUSTOM) {
                OutlinedTextField(
                    value = options.customInstructions,
                    onValueChange = { viewModel.updateHumanizeOptions(custom = it) },
                    label = { Text("Custom Rewriting Instructions") },
                    placeholder = { Text("e.g., Use punchy 10-word sentences, light humor, and tech metaphors...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VioletPrimary,
                        unfocusedBorderColor = NavyBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            // Fine-Tuning Accordion
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showFineTuning = !showFineTuning }
                    .border(1.dp, NavyBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Adjustable Controls & Nuance",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Text(if (showFineTuning) "Hide" else "Customize", fontSize = 12.sp, color = CyanAccent)
                    }

                    if (showFineTuning) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Tone Selector
                        OptionSelectorRow(
                            label = "Tone",
                            options = listOf("Balanced", "Warm", "Casual", "Professional"),
                            selected = options.tone,
                            onSelect = { viewModel.updateHumanizeOptions(tone = it) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Reading Difficulty
                        OptionSelectorRow(
                            label = "Reading Difficulty",
                            options = listOf("Easy", "Standard", "Advanced"),
                            selected = options.readingDifficulty,
                            onSelect = { viewModel.updateHumanizeOptions(difficulty = it) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sentence Length
                        OptionSelectorRow(
                            label = "Sentence Rhythm",
                            options = listOf("Varied", "Short & Punchy", "Flowing"),
                            selected = options.sentenceLength,
                            onSelect = { viewModel.updateHumanizeOptions(sentenceLength = it) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Vocabulary Simplicity
                        OptionSelectorRow(
                            label = "Vocabulary",
                            options = listOf("Simple", "Natural", "Rich"),
                            selected = options.vocabularySimplicity,
                            onSelect = { viewModel.updateHumanizeOptions(vocabulary = it) }
                        )
                    }
                }
            }

            // Error notice
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

            Spacer(modifier = Modifier.height(6.dp))

            // Humanize Button
            Button(
                onClick = { viewModel.humanizeText() },
                enabled = !isHumanizing && inputText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryGradient)
                    .testTag("humanize_action_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                if (isHumanizing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Humanizing Prose...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Humanize Text",
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

@Composable
private fun CyanAccentLight(): Color = Color(0xFF38BDF8)

@Composable
private fun OptionSelectorRow(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { opt ->
                val isSel = selected.equals(opt, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) CyanAccent else Color(0xFF1E293B))
                        .clickable { onSelect(opt) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = opt,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) NavyBackground else Color.White
                    )
                }
            }
        }
    }
}
