package com.example.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.database.HistoryEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.AiScoreOrange
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.HumanScoreEmerald
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.PrimaryGradient
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToChecker: () -> Unit,
    onNavigateToHumanizer: () -> Unit,
    onNavigateToAiStyle: () -> Unit,
    onNavigateToImprover: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToCollab: () -> Unit,
    onNavigateToSubscription: () -> Unit
) {
    val context = LocalContext.current
    val recentHistory by viewModel.recentHistory.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val firestoreUser by viewModel.firestoreUser.collectAsState()
    var quickInputText by remember { mutableStateOf("") }
    val quickWords = viewModel.getWordCount(quickInputText)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 20.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_humanizeai_foreground),
                        contentDescription = "HumanizeAI Logo",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "HumanizeAI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Understand, rewrite, and improve your text.",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanAccent
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("home_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                    // User Avatar / Auth button
                    IconButton(
                        onClick = onNavigateToAuth,
                        modifier = Modifier.testTag("home_auth_button")
                    ) {
                        if (currentUser != null) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(VioletPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (currentUser?.displayName?.take(1) ?: "U").uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Account",
                                tint = CyanAccent
                            )
                        }
                    }
                }
            }
        }

        // Feature Banner Chips (Chat, Collab, Pro)
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    BannerChip(
                        icon = Icons.Default.SupportAgent,
                        title = "AI Support Agent",
                        subtitle = "Context-aware help",
                        color = VioletPrimary,
                        onClick = onNavigateToChat
                    )
                }
                item {
                    BannerChip(
                        icon = Icons.Default.Group,
                        title = "Live Collab Room",
                        subtitle = "Real-time team editing",
                        color = CyanAccent,
                        onClick = onNavigateToCollab
                    )
                }
                item {
                    BannerChip(
                        icon = Icons.Default.WorkspacePremium,
                        title = "Pro Plan",
                        subtitle = "$20/mo or $240/yr",
                        color = HumanScoreEmerald,
                        onClick = onNavigateToSubscription
                    )
                }
            }
        }

        // Quick Text Input Card
        item {
            GlassCard(
                modifier = Modifier.testTag("home_quick_input_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Composer",
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
                                    quickInputText = text
                                    Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("home_paste_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = CyanAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paste", color = CyanAccent, fontSize = 12.sp)
                        }
                        if (quickInputText.isNotEmpty()) {
                            TextButton(onClick = { quickInputText = "" }) {
                                Text("Clear", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = quickInputText,
                    onValueChange = { quickInputText = it },
                    placeholder = {
                        Text(
                            "Type or paste your text here to analyze or rewrite...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("home_quick_text_field"),
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$quickWords words | ${quickInputText.length} chars",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (quickInputText.isNotBlank()) {
                                    viewModel.setCheckerInput(quickInputText)
                                }
                                onNavigateToChecker()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                            modifier = Modifier.testTag("home_quick_analyze_btn")
                        ) {
                            Text("Check AI", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (quickInputText.isNotBlank()) {
                                    viewModel.setHumanizeInput(quickInputText)
                                }
                                onNavigateToHumanizer()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            modifier = Modifier.testTag("home_quick_humanize_btn")
                        ) {
                            Text("Humanize", fontSize = 12.sp, color = NavyBackground)
                        }
                    }
                }
            }
        }

        // Section Title: Tools
        item {
            Text(
                text = "Four Powerful AI Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // 4 Main Feature Cards
        item {
            ToolCard(
                title = "AI Text Checker",
                badge = "Detector",
                description = "Scientific stylometric, burstiness & perplexity analysis with sentence highlights and honest likelihood meters.",
                icon = Icons.Default.Psychology,
                gradient = Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))),
                testTag = "home_card_checker",
                onClick = {
                    if (quickInputText.isNotBlank()) viewModel.setCheckerInput(quickInputText)
                    onNavigateToChecker()
                }
            )
        }

        item {
            ToolCard(
                title = "Humanize Text",
                badge = "Converter",
                description = "Transform AI or rigid text into natural human prose across 9 diverse styles while preserving core facts.",
                icon = Icons.Default.AutoAwesome,
                gradient = Brush.horizontalGradient(listOf(Color(0xFF06B6D4), Color(0xFF0284C7))),
                testTag = "home_card_humanizer",
                onClick = {
                    if (quickInputText.isNotBlank()) viewModel.setHumanizeInput(quickInputText)
                    onNavigateToHumanizer()
                }
            )
        }

        item {
            ToolCard(
                title = "AI Style Converter",
                badge = "Assistant",
                description = "Convert messy notes into structured executive summaries, technical guides, or academic research prose.",
                icon = Icons.Default.Transform,
                gradient = Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
                testTag = "home_card_aistyle",
                onClick = {
                    if (quickInputText.isNotBlank()) viewModel.setAiStyleInput(quickInputText)
                    onNavigateToAiStyle()
                }
            )
        }

        item {
            ToolCard(
                title = "Text Improvement Tool",
                badge = "Editor",
                description = "Grammar corrections, spelling, rephrasing, summarization, text expansion, and multilingual translation.",
                icon = Icons.Default.Translate,
                gradient = Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFFBE185D))),
                testTag = "home_card_improver",
                onClick = {
                    if (quickInputText.isNotBlank()) viewModel.setImproveInput(quickInputText)
                    onNavigateToImprover()
                }
            )
        }

        // Recent Activity Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (recentHistory.isNotEmpty()) {
                    TextButton(onClick = onNavigateToHistory) {
                        Text("View All", color = CyanAccent, fontSize = 13.sp)
                    }
                }
            }
        }

        if (recentHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No recent activity yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Check or humanize your first text to view history here.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        } else {
            items(recentHistory) { item ->
                RecentActivityCard(
                    item = item,
                    onClick = {
                        viewModel.reopenHistoryItem(item)
                        when (item.toolType) {
                            "CHECKER" -> onNavigateToChecker()
                            "HUMANIZE" -> onNavigateToHumanizer()
                            "AI_STYLE" -> onNavigateToAiStyle()
                            "IMPROVE" -> onNavigateToImprover()
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BannerChip(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun ToolCard(
    title: String,
    badge: String,
    description: String,
    icon: ImageVector,
    gradient: Brush,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, NavyBorder, RoundedCornerShape(18.dp))
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanAccent
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun RecentActivityCard(
    item: HistoryEntity,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(item.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, NavyBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val badgeColor = when (item.toolType) {
                        "CHECKER" -> VioletPrimary
                        "HUMANIZE" -> CyanAccent
                        "AI_STYLE" -> Color(0xFF3B82F6)
                        else -> Color(0xFFEC4899)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.toolType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.inputText.take(65) + if (item.inputText.length > 65) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }

            if (item.toolType == "CHECKER" && item.aiScore > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (item.aiScore >= 50) AiScoreOrange.copy(alpha = 0.2f) else HumanScoreEmerald.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${item.aiScore}% AI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.aiScore >= 50) AiScoreOrange else HumanScoreEmerald
                    )
                }
            }
        }
    }
}
