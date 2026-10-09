package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.database.AppDatabase
import com.example.data.database.HistoryEntity
import com.example.data.firestore.FirestoreChatMessage
import com.example.data.firestore.FirestoreCollaborationDoc
import com.example.data.firestore.FirestoreHistoryItem
import com.example.data.firestore.FirestoreRepository
import com.example.data.firestore.FirestoreUser
import com.example.data.linguistics.LinguisticAnalyzer
import com.example.data.model.AiStyleOptions
import com.example.data.model.AiStyleResult
import com.example.data.model.AiStyleType
import com.example.data.model.HumanizeOptions
import com.example.data.model.HumanizeResult
import com.example.data.model.HumanizeStyle
import com.example.data.model.ImproveResult
import com.example.data.model.ImproveToolType
import com.example.data.model.TextAnalysisResult
import com.example.data.repository.AiRepository
import com.example.data.repository.AppSettings
import com.example.data.repository.SettingsRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val historyDao = db.historyDao()
    val settingsRepository = SettingsRepository(application)
    val aiRepository = AiRepository(historyDao, settingsRepository)

    val authRepository = AuthRepository()
    val firestoreRepository = FirestoreRepository(application)

    val settings: StateFlow<AppSettings> = settingsRepository.settings

    // Auth & Cloud User State
    val currentUser: StateFlow<FirebaseUser?> = authRepository.authStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUser)

    private val _firestoreUser = MutableStateFlow<FirestoreUser?>(null)
    val firestoreUser: StateFlow<FirestoreUser?> = _firestoreUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    // Checker State
    private val _checkerInput = MutableStateFlow("")
    val checkerInput: StateFlow<String> = _checkerInput.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisResult = MutableStateFlow<TextAnalysisResult?>(null)
    val analysisResult: StateFlow<TextAnalysisResult?> = _analysisResult.asStateFlow()

    private val _checkerError = MutableStateFlow<String?>(null)
    val checkerError: StateFlow<String?> = _checkerError.asStateFlow()

    // Humanizer State
    private val _humanizeInput = MutableStateFlow("")
    val humanizeInput: StateFlow<String> = _humanizeInput.asStateFlow()

    private val _humanizeOptions = MutableStateFlow(HumanizeOptions())
    val humanizeOptions: StateFlow<HumanizeOptions> = _humanizeOptions.asStateFlow()

    private val _isHumanizing = MutableStateFlow(false)
    val isHumanizing: StateFlow<Boolean> = _isHumanizing.asStateFlow()

    private val _humanizeResult = MutableStateFlow<HumanizeResult?>(null)
    val humanizeResult: StateFlow<HumanizeResult?> = _humanizeResult.asStateFlow()

    private val _humanizeError = MutableStateFlow<String?>(null)
    val humanizeError: StateFlow<String?> = _humanizeError.asStateFlow()

    // AI Style Converter State
    private val _aiStyleInput = MutableStateFlow("")
    val aiStyleInput: StateFlow<String> = _aiStyleInput.asStateFlow()

    private val _aiStyleOptions = MutableStateFlow(AiStyleOptions())
    val aiStyleOptions: StateFlow<AiStyleOptions> = _aiStyleOptions.asStateFlow()

    private val _isConvertingAiStyle = MutableStateFlow(false)
    val isConvertingAiStyle: StateFlow<Boolean> = _isConvertingAiStyle.asStateFlow()

    private val _aiStyleResult = MutableStateFlow<AiStyleResult?>(null)
    val aiStyleResult: StateFlow<AiStyleResult?> = _aiStyleResult.asStateFlow()

    private val _aiStyleError = MutableStateFlow<String?>(null)
    val aiStyleError: StateFlow<String?> = _aiStyleError.asStateFlow()

    // Text Improvement State
    private val _improveInput = MutableStateFlow("")
    val improveInput: StateFlow<String> = _improveInput.asStateFlow()

    private val _selectedImproveTool = MutableStateFlow(ImproveToolType.GRAMMAR)
    val selectedImproveTool: StateFlow<ImproveToolType> = _selectedImproveTool.asStateFlow()

    private val _improveLanguage = MutableStateFlow("English")
    val improveLanguage: StateFlow<String> = _improveLanguage.asStateFlow()

    private val _isImproving = MutableStateFlow(false)
    val isImproving: StateFlow<Boolean> = _isImproving.asStateFlow()

    private val _improveResult = MutableStateFlow<ImproveResult?>(null)
    val improveResult: StateFlow<ImproveResult?> = _improveResult.asStateFlow()

    private val _improveError = MutableStateFlow<String?>(null)
    val improveError: StateFlow<String?> = _improveError.asStateFlow()

    // History & Search State
    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _historyFilter = MutableStateFlow("ALL") // ALL, CHECKER, HUMANIZE, AI_STYLE, IMPROVE
    val historyFilter: StateFlow<String> = _historyFilter.asStateFlow()

    val allHistory: StateFlow<List<HistoryEntity>> = historyDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentHistory: StateFlow<List<HistoryEntity>> = historyDao.getRecentHistory(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredHistory: StateFlow<List<HistoryEntity>> = combine(
        allHistory,
        _historyFilter,
        _historySearchQuery
    ) { list, filter, query ->
        list.filter { item ->
            val matchesFilter = (filter == "ALL" || item.toolType == filter)
            val matchesQuery = query.isBlank() ||
                    item.inputText.contains(query, ignoreCase = true) ||
                    item.outputText.contains(query, ignoreCase = true) ||
                    item.title.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Connection check state
    private val _connectionStatus = MutableStateFlow<String?>(null)
    val connectionStatus: StateFlow<String?> = _connectionStatus.asStateFlow()

    private val _isCheckingConnection = MutableStateFlow(false)
    val isCheckingConnection: StateFlow<Boolean> = _isCheckingConnection.asStateFlow()

    // Chatbot Support Agent State
    private val _chatMessages = MutableStateFlow<List<FirestoreChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<FirestoreChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatResponding = MutableStateFlow(false)
    val isChatResponding: StateFlow<Boolean> = _isChatResponding.asStateFlow()

    // Real-Time Collaboration State
    private val _currentCollabDoc = MutableStateFlow<FirestoreCollaborationDoc?>(null)
    val currentCollabDoc: StateFlow<FirestoreCollaborationDoc?> = _currentCollabDoc.asStateFlow()

    private val _collabStatus = MutableStateFlow<String?>(null)
    val collabStatus: StateFlow<String?> = _collabStatus.asStateFlow()

    init {
        // Observe Auth Changes and bind Firestore profile
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    val profile = firestoreRepository.getOrCreateUser(
                        userId = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: "HumanizeAI User",
                        photoUrl = user.photoUrl?.toString() ?: ""
                    )
                    _firestoreUser.value = profile

                    // Listen to profile updates
                    launch {
                        firestoreRepository.observeUser(user.uid).collect { updated ->
                            if (updated != null) _firestoreUser.value = updated
                        }
                    }

                    // Listen to chat messages
                    launch {
                        firestoreRepository.observeChatMessages(user.uid).collect { messages ->
                            _chatMessages.value = messages
                        }
                    }
                } else {
                    _firestoreUser.value = null
                    _chatMessages.value = emptyList()
                }
            }
        }
    }

    // --- AUTH ACTIONS ---
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _isSigningIn.value = true
            _authError.value = null
            val result = authRepository.signInWithGoogle(context)
            _isSigningIn.value = false
            result.fold(
                onSuccess = { user ->
                    _authError.value = null
                },
                onFailure = { error ->
                    _authError.value = error.localizedMessage ?: "Sign-in failed."
                }
            )
        }
    }

    fun signOut() {
        authRepository.signOut()
        _firestoreUser.value = null
    }

    // --- CUSTOM INSTRUCTIONS & PLAN ACTIONS ---
    fun updateCustomInstructions(instructions: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            firestoreRepository.updateCustomInstructions(user.uid, instructions)
            _firestoreUser.value = _firestoreUser.value?.copy(customInstructions = instructions)
        }
    }

    fun upgradePlan(plan: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val credits = if (plan == "annual") 2500L else 500L
            firestoreRepository.updatePlan(user.uid, plan, credits)
            _firestoreUser.value = _firestoreUser.value?.copy(plan = plan, credits = (_firestoreUser.value?.credits ?: 0L) + credits)
        }
    }

    // --- CONTEXT-AWARE CHATBOT ACTIONS ---
    fun sendChatMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank()) return

        val user = currentUser.value
        val userId = user?.uid ?: "local_user"
        val customPrompt = _firestoreUser.value?.customInstructions ?: ""

        val userMsg = FirestoreChatMessage(userId = userId, role = "user", content = trimmed)
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(userMsg)
        _chatMessages.value = currentList

        viewModelScope.launch {
            if (user != null) {
                firestoreRepository.saveChatMessage(userId, "user", trimmed)
            }

            _isChatResponding.value = true

            // Formulate conversation context for Gemini
            val contextBuilder = StringBuilder()
            contextBuilder.append("You are the intelligent support agent and expert writing advisor for HumanizeAI.\n")
            contextBuilder.append("You remember previous messages in the conversation, help with multi-step requests/bookings, explain AI detection heuristics (burstiness, perplexity, TTR), and advise on custom style rules.\n")
            if (customPrompt.isNotBlank()) {
                contextBuilder.append("User's Custom App Instructions:\n\"\"\"$customPrompt\"\"\"\n")
            }
            contextBuilder.append("\nConversation History:\n")
            _chatMessages.value.takeLast(6).forEach {
                contextBuilder.append("${if (it.role == "user") "User" else "Agent"}: ${it.content}\n")
            }
            contextBuilder.append("User: $trimmed\nAgent:")

            val aiResult = aiRepository.humanizeText(
                text = trimmed,
                options = HumanizeOptions(
                    style = HumanizeStyle.CUSTOM,
                    customInstructions = "Act as context-aware HumanizeAI support agent. Respond conversationally, concisely, and helpfully."
                )
            )

            val replyText = aiResult.fold(
                onSuccess = { it.humanizedText },
                onFailure = { "I'm here to assist with your text analysis, rewriting styles, and account settings. How can I assist you with your draft today?" }
            )

            val agentMsg = FirestoreChatMessage(userId = userId, role = "assistant", content = replyText)
            val updated = _chatMessages.value.toMutableList()
            updated.add(agentMsg)
            _chatMessages.value = updated

            if (user != null) {
                firestoreRepository.saveChatMessage(userId, "assistant", replyText)
            }
            _isChatResponding.value = false
        }
    }

    // --- REAL-TIME COLLABORATION ACTIONS ---
    fun createCollaborationSession(title: String, initialContent: String) {
        val user = currentUser.value
        val ownerId = user?.uid ?: "guest"
        val ownerName = user?.displayName ?: "Collaborator"

        viewModelScope.launch {
            _collabStatus.value = "Creating collaborative session..."
            try {
                val docId = firestoreRepository.createCollaborationSession(title, initialContent, ownerId, ownerName)
                joinCollaborationSession(docId)
                _collabStatus.value = "Session created: $docId"
            } catch (e: Exception) {
                _collabStatus.value = "Error creating session: ${e.localizedMessage}"
            }
        }
    }

    fun joinCollaborationSession(docId: String) {
        val user = currentUser.value
        viewModelScope.launch {
            _collabStatus.value = "Connecting to session..."
            try {
                if (user != null) {
                    firestoreRepository.joinCollaborationSession(docId, user.uid)
                }
                firestoreRepository.observeCollaborationSession(docId).collect { doc ->
                    _currentCollabDoc.value = doc
                    if (doc != null) {
                        _collabStatus.value = "Connected to ${doc.title}"
                    }
                }
            } catch (e: Exception) {
                _collabStatus.value = "Failed to connect: ${e.localizedMessage}"
            }
        }
    }

    fun updateCollaborationContent(docId: String, content: String) {
        val editorName = currentUser.value?.displayName ?: "Collaborator"
        viewModelScope.launch {
            firestoreRepository.updateCollaborationContent(docId, content, editorName)
        }
    }

    fun analyzeCollaborationContent(docId: String) {
        val doc = _currentCollabDoc.value ?: return
        viewModelScope.launch {
            val result = aiRepository.analyzeText(doc.content)
            result.onSuccess {
                firestoreRepository.updateCollaborationScores(docId, it.aiPercentage.toLong(), it.humanPercentage.toLong())
            }
        }
    }

    // --- CHECKER ACTIONS ---
    fun setCheckerInput(text: String) {
        _checkerInput.value = text
        _checkerError.value = null
    }

    fun clearCheckerInput() {
        _checkerInput.value = ""
        _analysisResult.value = null
        _checkerError.value = null
    }

    fun analyzeText() {
        val text = _checkerInput.value.trim()
        if (text.isBlank()) {
            _checkerError.value = "Please enter or paste text to analyze."
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            _checkerError.value = null
            val result = aiRepository.analyzeText(text)
            _isAnalyzing.value = false
            result.fold(
                onSuccess = { res ->
                    _analysisResult.value = res
                    currentUser.value?.let { user ->
                        firestoreRepository.saveCloudHistory(
                            userId = user.uid,
                            item = FirestoreHistoryItem(
                                userId = user.uid,
                                toolType = "CHECKER",
                                title = "AI Analysis: ${res.aiPercentage}% AI",
                                inputText = text,
                                outputText = res.overallAssessment,
                                aiScore = res.aiPercentage.toLong(),
                                humanScore = res.humanPercentage.toLong(),
                                wordCount = res.statistics.wordCount.toLong()
                            )
                        )
                    }
                },
                onFailure = { _checkerError.value = it.localizedMessage ?: "Analysis failed." }
            )
        }
    }

    // --- HUMANIZE ACTIONS ---
    fun setHumanizeInput(text: String) {
        _humanizeInput.value = text
        _humanizeError.value = null
    }

    fun setHumanizeStyle(style: HumanizeStyle) {
        _humanizeOptions.value = _humanizeOptions.value.copy(style = style)
    }

    fun updateHumanizeOptions(
        tone: String = _humanizeOptions.value.tone,
        difficulty: String = _humanizeOptions.value.readingDifficulty,
        sentenceLength: String = _humanizeOptions.value.sentenceLength,
        outputLength: String = _humanizeOptions.value.outputLength,
        vocabulary: String = _humanizeOptions.value.vocabularySimplicity,
        custom: String = _humanizeOptions.value.customInstructions
    ) {
        _humanizeOptions.value = _humanizeOptions.value.copy(
            tone = tone,
            readingDifficulty = difficulty,
            sentenceLength = sentenceLength,
            outputLength = outputLength,
            vocabularySimplicity = vocabulary,
            customInstructions = custom
        )
    }

    fun humanizeText() {
        val text = _humanizeInput.value.trim()
        if (text.isBlank()) {
            _humanizeError.value = "Please enter or paste text to humanize."
            return
        }

        viewModelScope.launch {
            _isHumanizing.value = true
            _humanizeError.value = null

            // Merge user custom instructions if present
            val userCustom = _firestoreUser.value?.customInstructions ?: ""
            val effectiveOptions = if (userCustom.isNotBlank() && _humanizeOptions.value.customInstructions.isBlank()) {
                _humanizeOptions.value.copy(customInstructions = userCustom)
            } else {
                _humanizeOptions.value
            }

            val result = aiRepository.humanizeText(text, effectiveOptions)
            _isHumanizing.value = false
            result.fold(
                onSuccess = { res ->
                    _humanizeResult.value = res
                    currentUser.value?.let { user ->
                        firestoreRepository.saveCloudHistory(
                            userId = user.uid,
                            item = FirestoreHistoryItem(
                                userId = user.uid,
                                toolType = "HUMANIZE",
                                title = "Humanized (${effectiveOptions.style.displayName})",
                                inputText = text,
                                outputText = res.humanizedText,
                                styleSelected = effectiveOptions.style.displayName,
                                wordCount = res.wordCountAfter.toLong()
                            )
                        )
                    }
                },
                onFailure = { _humanizeError.value = it.localizedMessage ?: "Humanization failed." }
            )
        }
    }

    fun replaceHumanizeInputWithOutput() {
        _humanizeResult.value?.let {
            _humanizeInput.value = it.humanizedText
            _humanizeResult.value = null
        }
    }

    // --- AI STYLE ACTIONS ---
    fun setAiStyleInput(text: String) {
        _aiStyleInput.value = text
        _aiStyleError.value = null
    }

    fun setAiStyleType(style: AiStyleType) {
        _aiStyleOptions.value = _aiStyleOptions.value.copy(style = style)
    }

    fun updateAiStyleOptions(
        formality: String = _aiStyleOptions.value.formalityLevel,
        detail: String = _aiStyleOptions.value.detailLevel,
        vocabulary: String = _aiStyleOptions.value.vocabularyLevel,
        outputLength: String = _aiStyleOptions.value.outputLength
    ) {
        _aiStyleOptions.value = _aiStyleOptions.value.copy(
            formalityLevel = formality,
            detailLevel = detail,
            vocabularyLevel = vocabulary,
            outputLength = outputLength
        )
    }

    fun convertAiStyle() {
        val text = _aiStyleInput.value.trim()
        if (text.isBlank()) {
            _aiStyleError.value = "Please enter or paste text to convert."
            return
        }

        viewModelScope.launch {
            _isConvertingAiStyle.value = true
            _aiStyleError.value = null
            val result = aiRepository.convertAiStyle(text, _aiStyleOptions.value)
            _isConvertingAiStyle.value = false
            result.fold(
                onSuccess = { res ->
                    _aiStyleResult.value = res
                    currentUser.value?.let { user ->
                        firestoreRepository.saveCloudHistory(
                            userId = user.uid,
                            item = FirestoreHistoryItem(
                                userId = user.uid,
                                toolType = "AI_STYLE",
                                title = "AI Style (${_aiStyleOptions.value.style.displayName})",
                                inputText = text,
                                outputText = res.aiStyledText,
                                styleSelected = _aiStyleOptions.value.style.displayName,
                                wordCount = res.wordCountAfter.toLong()
                            )
                        )
                    }
                },
                onFailure = { _aiStyleError.value = it.localizedMessage ?: "Style conversion failed." }
            )
        }
    }

    // --- IMPROVE ACTIONS ---
    fun setImproveInput(text: String) {
        _improveInput.value = text
        _improveError.value = null
    }

    fun setImproveTool(tool: ImproveToolType) {
        _selectedImproveTool.value = tool
    }

    fun setImproveLanguage(lang: String) {
        _improveLanguage.value = lang
    }

    fun improveText() {
        val text = _improveInput.value.trim()
        if (text.isBlank()) {
            _improveError.value = "Please enter text to improve."
            return
        }

        viewModelScope.launch {
            _isImproving.value = true
            _improveError.value = null
            val result = aiRepository.improveText(
                text = text,
                toolType = _selectedImproveTool.value,
                targetLanguage = _improveLanguage.value
            )
            _isImproving.value = false
            result.fold(
                onSuccess = { res ->
                    _improveResult.value = res
                    currentUser.value?.let { user ->
                        firestoreRepository.saveCloudHistory(
                            userId = user.uid,
                            item = FirestoreHistoryItem(
                                userId = user.uid,
                                toolType = "IMPROVE",
                                title = "Improved (${_selectedImproveTool.value.displayName})",
                                inputText = text,
                                outputText = res.improvedText,
                                styleSelected = _selectedImproveTool.value.displayName,
                                wordCount = LinguisticAnalyzer.splitWords(res.improvedText).size.toLong()
                            )
                        )
                    }
                },
                onFailure = { _improveError.value = it.localizedMessage ?: "Improvement failed." }
            )
        }
    }

    // --- HISTORY ACTIONS ---
    fun setHistoryFilter(filter: String) {
        _historyFilter.value = filter
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            historyDao.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyDao.clearAll()
        }
    }

    fun reopenHistoryItem(item: HistoryEntity) {
        when (item.toolType) {
            "CHECKER" -> _checkerInput.value = item.inputText
            "HUMANIZE" -> _humanizeInput.value = item.inputText
            "AI_STYLE" -> _aiStyleInput.value = item.inputText
            "IMPROVE" -> _improveInput.value = item.inputText
        }
    }

    // --- SETTINGS ACTIONS ---
    fun testBackendConnection(url: String) {
        viewModelScope.launch {
            _isCheckingConnection.value = true
            _connectionStatus.value = "Connecting to $url..."
            val result = aiRepository.testBackendHealth(url)
            _isCheckingConnection.value = false
            result.fold(
                onSuccess = { _connectionStatus.value = it },
                onFailure = { _connectionStatus.value = it.localizedMessage ?: "Failed to connect" }
            )
        }
    }

    fun getWordCount(text: String): Int {
        return LinguisticAnalyzer.splitWords(text).size
    }
}
