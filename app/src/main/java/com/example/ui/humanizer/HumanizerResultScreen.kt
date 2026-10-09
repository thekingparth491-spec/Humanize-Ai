package com.example.ui.humanizer

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.ui.components.GlassCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.HumanScoreEmerald
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.PrimaryGradient
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HumanizerResultScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val result by viewModel.humanizeResult.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Humanized, 1: Original Comparison

    if (result == null) {
        onNavigateBack()
        return
    }

    val res = result!!

    fun copyText() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Humanized Text", res.humanizedText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Humanized text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareText() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Humanized Text")
            putExtra(Intent.EXTRA_TEXT, res.humanizedText)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Humanized Text"))
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Humanized Output",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { copyText() }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White)
                    }
                    IconButton(onClick = { shareText() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                }
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
                .testTag("humanizer_result_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Header Card
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = res.options.style.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = res.changesSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${res.wordCountBefore}w → ${res.wordCountAfter}w",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HumanScoreEmerald
                        )
                    }
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = CyanAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyanAccent
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Humanized Result", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Original Comparison", fontWeight = FontWeight.SemiBold) }
                )
            }

            // Content Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NavyBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    if (selectedTab == 0) {
                        Text(
                            text = res.humanizedText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            lineHeight = 24.sp
                        )
                    } else {
                        Column {
                            Text(
                                text = "Original Input:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = res.originalText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Humanized Output:",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyanAccent
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = res.humanizedText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            // Disclaimer Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NavyBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Humanized output is crafted for authentic human rhythm and reading flow. We do not claim any rewritten text is guaranteed to bypass AI detection systems.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { copyText() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("humanize_copy_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NavyBackground)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Text", color = NavyBackground, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.replaceHumanizeInputWithOutput()
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("humanize_replace_input_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VioletPrimary)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = VioletPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Replace Input", color = VioletPrimary, fontSize = 13.sp)
                    }
                }

                OutlinedButton(
                    onClick = {
                        viewModel.humanizeText()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("humanize_rewrite_again_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rewrite Again", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
