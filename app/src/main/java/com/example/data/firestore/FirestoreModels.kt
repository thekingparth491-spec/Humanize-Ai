package com.example.data.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class FirestoreUser(
    @DocumentId val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val customInstructions: String = "",
    val plan: String = "free", // "free", "monthly", "annual"
    val credits: Long = 100L,
    val theme: String = "dark",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class FirestoreHistoryItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val toolType: String = "", // "CHECKER", "HUMANIZE", "AI_STYLE", "IMPROVE"
    val title: String = "",
    val inputText: String = "",
    val outputText: String = "",
    val styleSelected: String = "",
    val aiScore: Long = 0L,
    val humanScore: Long = 0L,
    val wordCount: Long = 0L,
    @ServerTimestamp val timestamp: Timestamp? = null
)

data class FirestoreCollaborationDoc(
    @DocumentId val id: String = "",
    val title: String = "Collaborative Session",
    val content: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val participantIds: List<String> = emptyList(),
    val lastEditedBy: String = "",
    val activeTool: String = "CHECKER",
    val aiScore: Long = 0L,
    val humanScore: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class FirestoreChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val role: String = "user", // "user", "assistant"
    val content: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)
