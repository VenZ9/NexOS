package dev.nexos.apps.runtimemanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nexos.runtime.PeFileInspector
import dev.nexos.runtime.PeMetadata
import dev.nexos.runtime.RuntimeProvider
import dev.nexos.runtime.RuntimeState
import dev.nexos.runtime.RuntimeStatus
import dev.nexos.runtime.WindowsRuntime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class RuntimeManagerUiState(
    val status: RuntimeStatus? = null,
    val state: RuntimeState = RuntimeState.NotInstalled,
    val isRunningDiagnostics: Boolean = false,
    val diagnosticLog: List<String> = emptyList(),
    val samplePeResult: PeMetadata? = null,
    val isMock: Boolean = false
)

class RuntimeManagerViewModel(
    private val runtimeProvider: RuntimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(RuntimeManagerUiState())
    val uiState: StateFlow<RuntimeManagerUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val runtime = runtimeProvider.activeRuntime.value
            val status = runtime.getStatus()
            _uiState.value = _uiState.value.copy(
                status = status,
                state = runtime.state.value,
                isMock = runtime.isMock
            )
        }
    }

    fun runDiagnostics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRunningDiagnostics = true)
            val logs = mutableListOf<String>()

            logs.add("[1/4] Probing host processor ABI...")
            val runtime = runtimeProvider.activeRuntime.value
            val status = runtime.getStatus()
            logs.add("      Detected Architecture: ${status.architecture}")

            logs.add("[2/4] Verifying graphics & Vulkan drivers...")
            logs.add("      Vulkan API Hardware Availability: ${if (status.vulkanAvailable) "Supported" else "Missing / Fallback"}")

            logs.add("[3/4] Probing native container binaries...")
            logs.add("      Wine glibc binary: ${if (status.wineInstalled) "Present" else "Missing"}")
            logs.add("      Box64 dynamic translator: ${if (status.box64Installed) "Present" else "Missing"}")

            logs.add("[4/4] Evaluating execution readiness...")
            if (runtime.isMock) {
                logs.add("      Mode: Simulated Environment [Mock Dev Mode]")
                logs.add("      STATUS: READY (Simulated execution enabled)")
            } else if (status.canLaunchRealWindowsBinaries) {
                logs.add("      STATUS: READY (Real container execution available)")
            } else {
                logs.add("      Missing components: ${status.missingComponents.joinToString(", ")}")
                logs.add("      STATUS: OFFLINE (Requires external Wine rootfs package)")
            }

            _uiState.value = _uiState.value.copy(
                status = status,
                isRunningDiagnostics = false,
                diagnosticLog = logs
            )
        }
    }

    fun switchRuntime(type: String) {
        runtimeProvider.setRuntimeType(type)
        refresh()
    }
}
