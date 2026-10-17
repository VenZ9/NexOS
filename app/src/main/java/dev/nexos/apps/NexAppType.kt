package dev.nexos.apps

enum class NexAppType(val defaultTitle: String, val isSingleInstance: Boolean = false) {
    FILE_MANAGER("File Manager", isSingleInstance = false),
    TEXT_EDITOR("Text Editor", isSingleInstance = false),
    SETTINGS("Settings", isSingleInstance = true),
    APP_LIBRARY("App Library", isSingleInstance = true),
    SYSTEM_INFO("System Information", isSingleInstance = true),
    RUNTIME_MANAGER("Runtime Manager", isSingleInstance = true),
    ABOUT_NEXOS("About NexOS", isSingleInstance = true),
    WINDOWS_EXE_RUNNER("Windows Executable Host", isSingleInstance = false)
}
