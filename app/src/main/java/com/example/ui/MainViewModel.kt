package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.DataStoreManager
import com.example.data.remote.model.SenderRule
import com.example.repository.EventRepository
import com.example.worker.ConfigSyncWorker
import com.example.worker.EventUploadWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class SettingsUiState(
    val pendingCount: Int = 0,
    val syncedCount: Int = 0,
    val deadLetterCount: Int = 0,
    val rules: List<SenderRule> = emptyList(),
    val jwtToken: String? = null,
    val deviceId: String? = null,
    val themeMode: Int = 0
)

class MainViewModel(
    application: Application,
    private val eventRepository: EventRepository,
    private val dataStoreManager: DataStoreManager
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<SettingsUiState> = combine(
        eventRepository.getPendingCountFlow(),
        eventRepository.getAcknowledgedCountFlow(),
        eventRepository.getDeadLetterCountFlow(),
        dataStoreManager.senderRulesFlow,
        dataStoreManager.jwtTokenFlow,
        dataStoreManager.deviceIdFlow,
        dataStoreManager.themeModeFlow
    ) { args: Array<Any?> ->
        SettingsUiState(
            pendingCount = args[0] as Int,
            syncedCount = args[1] as Int,
            deadLetterCount = args[2] as Int,
            rules = args[3] as List<SenderRule>,
            jwtToken = args[4] as String?,
            deviceId = args[5] as String?,
            themeMode = args[6] as Int
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    init {
        viewModelScope.launch {
            dataStoreManager.getDeviceId() 
        }
    }

    fun syncNow() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val workRequest = OneTimeWorkRequestBuilder<EventUploadWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
            
        workManager.enqueue(workRequest)
    }

    fun refreshWhitelist() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val workRequest = OneTimeWorkRequestBuilder<ConfigSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
            
        workManager.enqueue(workRequest)
    }

    fun logout() {
        viewModelScope.launch {
            dataStoreManager.clearTokens()
        }
    }

    fun login(token: String) {
        viewModelScope.launch {
            dataStoreManager.setTokens(token, "DEMO_REFRESH_TOKEN_123")
        }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            dataStoreManager.setThemeMode(mode)
        }
    }

    fun addSenderRule(rule: SenderRule) {
        viewModelScope.launch {
            val currentRules = uiState.value.rules.toMutableList()
            if (!currentRules.contains(rule)) {
                currentRules.add(rule)
                dataStoreManager.setSenderRules(currentRules)
            }
        }
    }

    fun removeSenderRule(rule: SenderRule) {
        viewModelScope.launch {
            val currentRules = uiState.value.rules.toMutableList()
            if (currentRules.remove(rule)) {
                dataStoreManager.setSenderRules(currentRules)
            }
        }
    }

    companion object {
        fun provideFactory(
            application: Application,
            eventRepository: EventRepository,
            dataStoreManager: DataStoreManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(application, eventRepository, dataStoreManager) as T
            }
        }
    }
}
