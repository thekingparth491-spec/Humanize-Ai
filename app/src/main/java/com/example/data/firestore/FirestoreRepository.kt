package com.example.data.firestore

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val db: FirebaseFirestore
) {
    // Secondary constructor resolving named database ID
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    // --- USER PROFILE & PREFERENCES ---

    suspend fun getOrCreateUser(userId: String, email: String, displayName: String, photoUrl: String): FirestoreUser {
        val docRef = db.collection("users").document(userId)
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            return snapshot.toObject(FirestoreUser::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: FirestoreUser(userId = userId, email = email, displayName = displayName)
        }

        val newUser = FirestoreUser(
            userId = userId,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl,
            customInstructions = "",
            plan = "free",
            credits = 50L,
            theme = "dark"
        )
        val data = mapOf(
            "userId" to userId,
            "displayName" to displayName,
            "email" to email,
            "photoUrl" to photoUrl,
            "customInstructions" to "",
            "plan" to "free",
            "credits" to 50L,
            "theme" to "dark",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data).await()
        return newUser
    }

    fun observeUser(userId: String): Flow<FirestoreUser?> = callbackFlow {
        val listener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(FirestoreUser::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                trySend(user)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateCustomInstructions(userId: String, instructions: String) {
        db.collection("users").document(userId)
            .update(
                mapOf(
                    "customInstructions" to instructions,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    suspend fun updatePlan(userId: String, plan: String, creditsToAdd: Long) {
        db.collection("users").document(userId)
            .update(
                mapOf(
                    "plan" to plan,
                    "credits" to FieldValue.increment(creditsToAdd),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    suspend fun deductCredit(userId: String): Boolean {
        return try {
            db.collection("users").document(userId)
                .update(
                    mapOf(
                        "credits" to FieldValue.increment(-1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- CLOUD HISTORY ---

    suspend fun saveCloudHistory(userId: String, item: FirestoreHistoryItem) {
        val col = db.collection("users").document(userId).collection("history")
        val data = mapOf(
            "userId" to userId,
            "toolType" to item.toolType,
            "title" to item.title,
            "inputText" to item.inputText,
            "outputText" to item.outputText,
            "styleSelected" to item.styleSelected,
            "aiScore" to item.aiScore,
            "humanScore" to item.humanScore,
            "wordCount" to item.wordCount,
            "timestamp" to FieldValue.serverTimestamp()
        )
        col.add(data).await()
    }

    fun observeCloudHistory(userId: String): Flow<List<FirestoreHistoryItem>> = callbackFlow {
        val listener = db.collection("users").document(userId).collection("history")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(FirestoreHistoryItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    // --- CONTEXT-AWARE CHATBOT MESSAGES ---

    suspend fun saveChatMessage(userId: String, role: String, content: String) {
        val data = mapOf(
            "userId" to userId,
            "role" to role,
            "content" to content,
            "timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(userId).collection("chat_messages").add(data).await()
    }

    fun observeChatMessages(userId: String): Flow<List<FirestoreChatMessage>> = callbackFlow {
        val listener = db.collection("users").document(userId).collection("chat_messages")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(FirestoreChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    // --- REAL-TIME COLLABORATION SESSIONS ---

    suspend fun createCollaborationSession(
        title: String,
        initialContent: String,
        ownerId: String,
        ownerName: String
    ): String {
        val docRef = db.collection("collaborations").document()
        val data = mapOf(
            "title" to title,
            "content" to initialContent,
            "ownerId" to ownerId,
            "ownerName" to ownerName,
            "participantIds" to listOf(ownerId),
            "lastEditedBy" to ownerName,
            "activeTool" to "CHECKER",
            "aiScore" to 0L,
            "humanScore" to 0L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data).await()
        return docRef.id
    }

    fun observeCollaborationSession(docId: String): Flow<FirestoreCollaborationDoc?> = callbackFlow {
        val listener = db.collection("collaborations").document(docId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val doc = snapshot?.toObject(FirestoreCollaborationDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                trySend(doc)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateCollaborationContent(docId: String, content: String, editorName: String) {
        db.collection("collaborations").document(docId).update(
            mapOf(
                "content" to content,
                "lastEditedBy" to editorName,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun updateCollaborationScores(docId: String, aiScore: Long, humanScore: Long) {
        db.collection("collaborations").document(docId).update(
            mapOf(
                "aiScore" to aiScore,
                "humanScore" to humanScore,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun joinCollaborationSession(docId: String, userId: String) {
        db.collection("collaborations").document(docId).update(
            "participantIds", FieldValue.arrayUnion(userId)
        ).await()
    }
}
