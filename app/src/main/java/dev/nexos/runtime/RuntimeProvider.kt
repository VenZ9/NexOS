package dev.nexos.runtime

import android.content.Context
import dev.nexos.runtime.mock.MockWindowsRuntime
import dev.nexos.runtime.real.RealWindowsRuntime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RuntimeProvider(private val context: Context) {

    private val realRuntime: RealWindowsRuntime by lazy { RealWindowsRuntime(context) }
    private val mockRuntime: MockWindowsRuntime by lazy { MockWindowsRuntime() }

    private val _activeRuntime = MutableStateFlow<WindowsRuntime>(realRuntime)
    val activeRuntime: StateFlow<WindowsRuntime> = _activeRuntime.asStateFlow()

    fun setRuntimeType(type: String) {
        if (type.equals("MOCK", ignoreCase = true)) {
            _activeRuntime.value = mockRuntime
        } else {
            _activeRuntime.value = realRuntime
        }
    }

    fun getRealRuntime(): RealWindowsRuntime = realRuntime
    fun getMockRuntime(): MockWindowsRuntime = mockRuntime
}
