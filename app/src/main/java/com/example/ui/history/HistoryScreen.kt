package com.example.ui.history

import android.content.ClipData
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.database.HistoryEntity
import com.example.ui.components.AppHeader
import com.example.ui.theme.AiScoreOrange
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.HumanScoreEmerald
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChecker: () -> Unit,
    onNavigateToHumanizer: () -> Unit,
    onNavigateToAiStyle: () -> Unit,
    onNavigateToImprover: () -> Unit
) {
    val context = LocalContext.current
    val historyList by viewModel.filteredHistory.collectAsState()
    val currentFilter by viewModel.historyFilter.collectAsState()
    val searchQuery by viewModel.historySearchQuery.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }

    val filterOptions = listOf(
        "ALL" to "All",
        "CHECKER" to "AI Checker",
        "HUMANIZE" to "Humanize",
        "AI_STYLE" to "AI Style",
        "IMPROVE" to "Improve"
    )

    fun copyItemText(item: HistoryEntity) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val textToCopy = if (item.outputText.isNotBlank()) item.outputText else item.inputText
        val clip = ClipData.newPlainText("History Item", textToCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Local History",
                onBackClick = onNavigateBack,
                actions = {
                    if (historyList.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirm = true },
                            modifier = Modifier.testTag("history_clear_all_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = Color(0xFFFCA5A5))
                        }
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
                .testTag("history_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setHistorySearchQuery(it) },
                placeholder = { Text("Search saved history...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanAccent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = NavyBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            // Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filterOptions) { (key, label) ->
                    val isSelected = currentFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setHistoryFilter(key) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VioletPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) VioletPrimary else NavyBorder
                        )
                    )
                }
            }

            // History List
            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching records found" else "History is empty",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Actions you perform are securely stored on-device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historyList, key = { it.id }) { item ->
                        HistoryItemCard(
                            item = item,
                            onCopy = { copyItemText(item) },
                            onDelete = { viewModel.deleteHistoryItem(item.id) },
                            onReopen = {
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
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All History?", color = Color.White) },
            text = { Text("This will permanently delete all saved analyses and rewritten drafts from this device.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearConfirm = false
                        Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete All", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = CyanAccent)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun HistoryItemCard(
    item: HistoryEntity,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onReopen: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(item.timestamp))

    val badgeColor = when (item.toolType) {
        "CHECKER" -> VioletPrimary
        "HUMANIZE" -> CyanAccent
        "AI_STYLE" -> Color(0xFF3B82F6)
        else -> Color(0xFFEC4899)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NavyBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Tool badge, date, action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.toolType,
                            fontSize = 11.sp,
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

                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyanAccent, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onReopen, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Reopen", tint = VioletPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFCA5A5), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title or score
            if (item.toolType == "CHECKER" && item.aiScore > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.aiScore}% AI Likelihood",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (item.aiScore >= 50) AiScoreOrange else HumanScoreEmerald
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "· ${item.outputText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            } else if (item.title.isNotBlank()) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Input preview
            Text(
                text = item.inputText.take(120) + if (item.inputText.length > 120) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}
