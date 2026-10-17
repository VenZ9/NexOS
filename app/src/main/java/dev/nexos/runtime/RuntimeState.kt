package dev.nexos.runtime

sealed class RuntimeState {
    object NotInstalled : RuntimeState()
    object Checking : RuntimeState()
    data class Initializing(val progress: Float, val message: String) : RuntimeState()
    data class Ready(val status: RuntimeStatus) : RuntimeState()
    data class Running(val app: InstalledApp, val pid: Int = -1) : RuntimeState()
    object Stopped : RuntimeState()
    data class Error(val title: String, val message: String, val details: String? = null) : RuntimeState()

    val isRunning: Boolean
        get() = this is Running

    val isReady: Boolean
        get() = this is Ready
}
