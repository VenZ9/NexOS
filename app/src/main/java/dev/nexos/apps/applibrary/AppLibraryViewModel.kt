package dev.nexos.apps.applibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nexos.apps.AppDescriptor
import dev.nexos.apps.AppRegistry
import dev.nexos.runtime.InstalledApp
import dev.nexos.runtime.RuntimeProvider
import dev.nexos.runtime.RuntimeStatus
import dev.nexos.runtime.WindowsRuntime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppFilter {
    ALL,
    BUILT_IN,
    WINDOWS_EXE
}

data class AppLibraryUiState(
    val builtInApps: List<AppDescriptor> = AppRegistry.builtInApps,
    val windowsApps: List<InstalledApp> = emptyList(),
    val runtimeStatus: RuntimeStatus? = null,
    val isMock: Boolean = false,
    val selectedFilter: AppFilter = AppFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedAppId: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AppLibraryViewModel(
    private val runtimeProvider: RuntimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppLibraryUiState())
    val uiState: StateFlow<AppLibraryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val runtime = runtimeProvider.activeRuntime.value
            val status = runtime.getStatus()
            val apps = runtime.listInstalledApplications()

            _uiState.value = _uiState.value.copy(
                windowsApps = apps,
                runtimeStatus = status,
                isMock = runtime.isMock,
                isLoading = false
            )
        }
    }

    fun setFilter(filter: AppFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectApp(appId: String?) {
        _uiState.value = _uiState.value.copy(selectedAppId = appId)
    }

    fun launchWindowsApp(app: InstalledApp, onLaunched: (Int) -> Unit) {
        viewModelScope.launch {
            val runtime = runtimeProvider.activeRuntime.value
            val result = runtime.launchApplication(app.id)
            result.onSuccess { pid ->
                _uiState.value = _uiState.value.copy(
                    successMessage = "Started ${app.name} (PID: $pid)"
                )
                onLaunched(pid)
                refresh()
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    errorMessage = it.message ?: "Failed to launch application"
                )
            }
        }
    }

    fun deleteWindowsApp(app: InstalledApp) {
        viewModelScope.launch {
            val runtime = runtimeProvider.activeRuntime.value
            runtime.deleteApplication(app.id)
            refresh()
        }
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
