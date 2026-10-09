package com.example.ui.collab

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.ui.components.AppHeader
import com.example.ui.components.DualLikelihoodBar
import com.example.ui.components.GlassCard
import com.example.ui.theme.AiScoreOrange
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.HumanScoreEmerald
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.PrimaryGradient
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CollaborationScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val collabDoc by viewModel.currentCollabDoc.collectAsState()
    val collabStatus by viewModel.collabStatus.collectAsState()

    var joinCodeInput by remember { mutableStateOf("") }
    var newRoomTitle by remember { mutableStateOf("Team Review Draft") }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Live Collaboration",
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
                .testTag("collaboration_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (collabDoc == null) {
                // Not in a room: Create or Join
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Real-Time Collaboration",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "Work together on essays, press releases, or reports. Edits and AI detector scores update across all devices simultaneously in real time via Firestore.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Create New Room Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NavyBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Create Shared Session", fontWeight = FontWeight.Bold, color = Color.White)

                        OutlinedTextField(
                            value = newRoomTitle,
                            onValueChange = { newRoomTitle = it },
                            label = { Text("Session Title") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = NavyBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Button(
                            onClick = {
                                viewModel.createCollaborationSession(
                                    title = newRoomTitle,
                                    initialContent = "Paste collaborative draft here for team review and joint AI checking..."
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryGradient)
                                .testTag("create_collab_room_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create & Join Room", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Join Existing Room Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NavyBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Join via Room Code / Document ID", fontWeight = FontWeight.Bold, color = Color.White)

                        OutlinedTextField(
                            value = joinCodeInput,
                            onValueChange = { joinCodeInput = it },
                            placeholder = { Text("Paste Room Code / Document ID") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VioletPrimary,
                                unfocusedBorderColor = NavyBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (joinCodeInput.isNotBlank()) {
                                    viewModel.joinCollaborationSession(joinCodeInput.trim())
                                }
                            },
                            enabled = joinCodeInput.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("join_collab_room_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connect to Session", color = CyanAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (collabStatus != null) {
                    Text(
                        text = collabStatus ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanAccent,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            } else {
                // Inside an Active Room
                val doc = collabDoc!!

                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(HumanScoreEmerald)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = doc.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Code: ${doc.id} · ${doc.participantIds.size} participant(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Collab Room ID", doc.id)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Room Code copied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Room Code", tint = CyanAccent)
                        }
                    }
                }

                // Collaborative Live Editor
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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Live Shared Document", fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text("Last edited by ${doc.lastEditedBy}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = doc.content,
                            onValueChange = { newText ->
                                viewModel.updateCollaborationContent(doc.id, newText)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .testTag("collab_live_editor"),
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
                    }
                }

                // Live Shared AI Likelihood Meter
                if (doc.aiScore > 0L || doc.humanScore > 0L) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NavyBorder, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161F33))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Shared AI Likelihood Assessment",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            DualLikelihoodBar(
                                aiScore = doc.aiScore.toInt(),
                                humanScore = doc.humanScore.toInt()
                            )
                        }
                    }
                }

                // Shared Action Buttons
                Button(
                    onClick = { viewModel.analyzeCollaborationContent(doc.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PrimaryGradient)
                        .testTag("collab_analyze_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyze Shared Draft (Updates Live)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
