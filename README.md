# NexOS

**NexOS** is a native Android application that provides a refined, Windows-inspired desktop environment and establishes an extensible compatibility architecture for running x86 / x64 Windows (`.exe` / `.msi`) applications on Android.

---

## Highlights

- **Native Android Experience**: Built in 100% Kotlin with Jetpack Compose and Material 3.
- **Fluent Desktop Shell**: Responsive desktop workspace, customizable wallpapers, taskbar with Start button, running app indicators, live system clock, and quick-action system tray.
- **Centralized Window Manager**: Multi-window system supporting simultaneous internal applications with drag, resize, minimize, maximize/restore, focus, and z-order activation.
- **Built-in Applications**:
  - **File Manager**: Browse user folders using the Android Storage Access Framework (SAF), navigate virtual C: drive, and import `.exe` installers.
  - **Text Editor (Notepad)**: Edit and save plain-text documents directly through Android document APIs.
  - **Settings**: Configure appearance, wallpapers, taskbar alignments, and runtime modes.
  - **App Library**: Browse built-in tools and imported Windows applications with honest status indicators.
  - **System Information**: Real-time hardware specifications, CPU architecture, memory, and Android platform diagnostics.
  - **Runtime Manager**: Wine 9.x / Box64 status dashboard with live PE inspection and diagnostics.
  - **About NexOS**: System information and open-source acknowledgements.
- **Windows Compatibility Layer (Real & Mock)**:
  - **Real Runtime Adapter**: Probes ARM64 host ABI, Vulkan hardware drivers, Wine glibc binaries, and Box64 dynamic translators.
  - **Safe PE Binary Inspector**: Validates DOS headers, parses COFF machine architectures (`x86`, `x64`, `ARM64`), verifies subsystems, and calculates SHA-256 integrity hashes.
  - **Mock Simulator (Dev Mode)**: Clearly labeled simulator allowing complete UI, window management, and installer testing without requiring 1GB container packages on the phone.
- **Zero Backend / Complete Privacy**: No telemetry, no accounts, no subscriptions, and no mandatory API keys.

---

## Architecture Overview

```
dev.nexos/
├── NexApplication.kt                # Application singleton & repository coordinator
├── MainActivity.kt                  # Edge-to-edge Compose host & back handler
├── desktop/                         # Desktop shell, workspace, taskbar, start menu, flyouts
│   ├── DesktopScreen.kt
│   ├── DesktopViewModel.kt
│   └── components/
│       ├── DesktopWorkspace.kt
│       ├── DesktopShortcutView.kt
│       ├── Taskbar.kt
│       ├── StartMenu.kt
│       ├── SystemTrayFlyout.kt
│       ├── ContextMenu.kt
│       └── WallpaperView.kt
├── windows/                         # Window manager & interactive window frames
│   ├── WindowManager.kt
│   ├── WindowInstance.kt
│   ├── WindowState.kt
│   └── components/
│       ├── WindowFrame.kt
│       ├── WindowTitleBar.kt
│       └── ResizeHandles.kt
├── apps/                            # Built-in native applications
│   ├── AppRegistry.kt
│   ├── filemanager/
│   ├── texteditor/
│   ├── settings/
│   ├── applibrary/
│   ├── systeminfo/
│   ├── runtimemanager/
│   ├── about/
│   └── windowsrunner/
├── runtime/                         # Windows compatibility runtime
│   ├── WindowsRuntime.kt            # Core runtime interface
│   ├── RuntimeState.kt              # State flow sealed classes
│   ├── RuntimeStatus.kt             # Engine status & component checklist
│   ├── PeFileInspector.kt           # Safe PE binary parser & hash calculator
│   ├── real/RealWindowsRuntime.kt   # Real Wine 9.x / Box64 adapter
│   ├── mock/MockWindowsRuntime.kt   # Developer mock simulator
│   └── RuntimeProvider.kt          # Active runtime selector
├── data/                            # Preferences & storage repositories
│   ├── preferences/UserPreferencesRepository.kt
│   └── repository/FileRepository.kt
└── ui/theme/                        # Fluent dark theme, typography, & colors
```

---

## Cloud CI/CD & GitHub Actions

NexOS is configured with GitHub Actions to compile and test the application in the cloud on every commit and pull request. Development, testing, and downloading the APK is fully supported directly from an Android phone without needing a local PC.

### How to Download the Debug APK on an Android Phone:

1. Open this repository on GitHub in your phone's web browser or the GitHub Mobile app:  
   `https://github.com/VenZ9/NexOS`
2. Navigate to the **Actions** tab.
3. Tap the latest workflow run under **Build NexOS Debug APK**.
4. Scroll to the **Artifacts** section at the bottom.
5. Tap **nexos-debug-apk** to download the ZIP file containing `app-debug.apk`.
6. Open the ZIP on your phone, extract `app-debug.apk`, and install it.

### Triggering a Manual Build from Your Phone:
1. In the **Actions** tab, select **Build NexOS Debug APK** on the left menu.
2. Tap the **Run workflow** dropdown button.
3. Select the `main` branch and tap **Run workflow**.

---

## Local Compilation (Optional)

If you have an environment with the Android SDK (or run via Termux/Ubuntu):

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug
```

Output APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Windows Compatibility Audit & Documentation

For a detailed analysis of Wine, Box64, PRoot, Turnip/Zink drivers, licensing, and native integration roadmaps, see:
[`docs/WINDOWS_RUNTIME_AUDIT.md`](docs/WINDOWS_RUNTIME_AUDIT.md)

---

## License

NexOS is open-source software licensed under the Apache 2.0 License.
Windows compatibility dependencies (Wine, Box64, DXVK) maintain their respective upstream licenses (LGPL v2.1, MIT, Zlib).
