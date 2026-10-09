package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.HistoryEntity
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

    val settings: StateFlow<AppSettings> = settingsRepository.settings

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
                onSuccess = { _analysisResult.value = it },
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
            val result = aiRepository.humanizeText(text, _humanizeOptions.value)
            _isHumanizing.value = false
            result.fold(
                onSuccess = { _humanizeResult.value = it },
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
                onSuccess = { _aiStyleResult.value = it },
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
                onSuccess = { _improveResult.value = it },
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
            "CHECKER" -> {
                _checkerInput.value = item.inputText
            }
            "HUMANIZE" -> {
                _humanizeInput.value = item.inputText
            }
            "AI_STYLE" -> {
                _aiStyleInput.value = item.inputText
            }
            "IMPROVE" -> {
                _improveInput.value = item.inputText
            }
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
