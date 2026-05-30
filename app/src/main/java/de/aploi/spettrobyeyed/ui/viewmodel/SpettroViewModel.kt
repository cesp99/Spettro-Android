package de.aploi.spettrobyeyed.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.aploi.spettrobyeyed.data.ConnectionPrefs
import de.aploi.spettrobyeyed.data.SpettroRepository
import de.aploi.spettrobyeyed.data.models.SpettroEvent
import de.aploi.spettrobyeyed.data.models.SpettroStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val host: String, val apiKey: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

class SpettroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SpettroRepository()
    val prefs = ConnectionPrefs(application)

    private val _events = MutableStateFlow<List<SpettroEvent>>(emptyList())
    val events: StateFlow<List<SpettroEvent>> = _events.asStateFlow()

    private val _status = MutableStateFlow(SpettroStatus())
    val status: StateFlow<SpettroStatus> = _status.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private var sseJob: Job? = null
    private var statusPollJob: Job? = null

    init {
        tryAutoConnect()
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun tryAutoConnect() {
        viewModelScope.launch {
            val savedHost = prefs.host.first()
            val savedKey = prefs.apiKey.first()
            if (savedHost.isNotEmpty() && savedKey.isNotEmpty()) {
                connect(savedHost, savedKey)
            }
        }
    }

    fun connect(rawHost: String, apiKey: String) {
        val host = rawHost
            .removePrefix("https://")
            .removePrefix("http://")
            .trimEnd('/')
        viewModelScope.launch {
            _connectionState.value = ConnectionState.Connecting
            repository.getStatus(host, apiKey).fold(
                onSuccess = { status ->
                    _status.value = status
                    _connectionState.value = ConnectionState.Connected(host, apiKey)
                    prefs.save(host, apiKey)
                    startSseStream(host, apiKey)
                    startStatusPolling(host, apiKey)
                },
                onFailure = { e ->
                    val msg = when {
                        e.message?.contains("Failed to connect") == true ->
                            "Cannot reach $host — is /remote running on the computer?"
                        e.message?.contains("401") == true || e.message?.contains("403") == true ->
                            "Invalid API key — copy the Bearer token from /remote status"
                        e.message?.contains("timeout", ignoreCase = true) == true ->
                            "Connection timed out — check host and network"
                        else -> e.message ?: "Connection failed"
                    }
                    _connectionState.value = ConnectionState.Error(msg)
                }
            )
        }
    }

    fun disconnect() {
        sseJob?.cancel()
        statusPollJob?.cancel()
        _connectionState.value = ConnectionState.Disconnected
        _events.value = emptyList()
        _status.value = SpettroStatus()
        viewModelScope.launch { prefs.clear() }
    }

    fun sendMessage(message: String) {
        val state = _connectionState.value as? ConnectionState.Connected ?: return
        viewModelScope.launch {
            repository.sendMessage(state.host, state.apiKey, message).fold(
                onSuccess = { result ->
                    if (result.error.isNotEmpty()) {
                        _snackbarMessage.value = result.error
                    } else if (result.queued) {
                        _snackbarMessage.value = "Message queued — agent is busy"
                    }
                },
                onFailure = { e -> _snackbarMessage.value = "Send failed: ${e.message}" }
            )
        }
    }

    fun interrupt() {
        val state = _connectionState.value as? ConnectionState.Connected ?: return
        viewModelScope.launch {
            repository.interrupt(state.host, state.apiKey)
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    private fun startSseStream(host: String, apiKey: String) {
        sseJob?.cancel()
        sseJob = viewModelScope.launch {
            var retryDelay = 1_000L
            while (isActive && _connectionState.value is ConnectionState.Connected) {
                try {
                    repository.streamEvents(host, apiKey)
                        .onEach { result ->
                            result.onSuccess { event ->
                                processEvent(event)
                                retryDelay = 1_000L
                            }
                        }
                        .catch { }
                        .collect { }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // ignore, retry below
                }

                if (isActive && _connectionState.value is ConnectionState.Connected) {
                    delay(retryDelay)
                    retryDelay = minOf(retryDelay * 2, 30_000L)
                }
            }
        }
    }

    private fun startStatusPolling(host: String, apiKey: String) {
        statusPollJob?.cancel()
        statusPollJob = viewModelScope.launch {
            while (isActive && _connectionState.value is ConnectionState.Connected) {
                delay(15_000)
                repository.getStatus(host, apiKey).onSuccess { status ->
                    _status.update {
                        it.copy(
                            messagesCount = status.messagesCount,
                            tokensUsed = status.tokensUsed
                        )
                    }
                }
            }
        }
    }

    private fun processEvent(event: SpettroEvent) {
        _events.update { current ->
            if (event.seq > 0 && current.any { it.seq == event.seq }) current
            else current + event
        }
        if (event is SpettroEvent.State) {
            _status.update {
                it.copy(
                    thinking = event.thinking,
                    mode = event.mode,
                    activeAgent = event.activeAgent
                )
            }
        }
    }
}
