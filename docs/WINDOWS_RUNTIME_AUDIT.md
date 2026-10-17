# NexOS Windows Runtime Architecture & Compatibility Audit

## Executive Summary

NexOS provides an authentic, responsive desktop environment on Android and establishes the foundation for executing compatible x86 / x64 Windows applications. Executing native Windows binaries on modern Android devices requires overcoming three fundamental architectural boundaries:

1. **CPU Architecture**: Standard Android mobile devices use ARM64 (`aarch64`) processors with RISC instruction sets, whereas Windows `.exe` applications are compiled for Intel/AMD x86 or x86_64 CISC instruction sets.
2. **Operating System ABI**: Android runs a Linux kernel with the Bionic libc runtime, whereas Windows applications link against Win32/Win64 system DLLs (`kernel32.dll`, `user32.dll`, `ntdll.dll`, `gdi32.dll`).
3. **Graphics & Audio Pipeline**: Windows applications issue Direct3D (DirectX 9/10/11/12) and GDI drawing calls, while Android provides Vulkan and OpenGL ES drivers.

---

## 1. Upstream Project Research & Candidate Architecture

Our research investigated existing open-source Android Windows compatibility projects, specifically:
- **Winlator** (`brunodev85/winlator`, reference: `winebox64/winlator`)
- **Box86 & Box64** (created by Sébastien Chevalier / `ptitSeb`)
- **Wine** (Wine Is Not an Emulator, upstream WineHQ)
- **PRoot / proot-distro** (chroot/ptrace emulation for glibc userland)
- **Mesa Turnip / Zink / DXVK** (DirectX-to-Vulkan and userland Adreno Vulkan drivers)

### Stack Diagram

```text
+-------------------------------------------------------------+
|              Windows Executable (.exe / .msi)               |
+-------------------------------------------------------------+
|           Wine 9.x Windows API Translation Layer            |
|       (kernel32.dll, user32.dll, ntdll.dll, shell32.dll)     |
+-------------------------------------------------------------+
|             DXVK 2.x / VKD3D (DirectX -> Vulkan)            |
+-------------------------------------------------------------+
|          Box64 Dynamic Binary Translator (x86_64 -> ARM64)  |
|          Box86 Dynamic Binary Translator (x86 -> ARM)       |
+-------------------------------------------------------------+
|             glibc Linux Container Userland (PRoot)          |
+-------------------------------------------------------------+
|      X11 / Wayland Display Server -> Android SurfaceView    |
+-------------------------------------------------------------+
|            Android Bionic Kernel & Adreno/Mali GPU          |
+-------------------------------------------------------------+
```

---

## 2. Licensing and Redistribution Audit

| Component | Upstream Origin | License | Redistribution Terms & Compliance Requirements |
| :--- | :--- | :--- | :--- |
| **Wine** | WineHQ (`winehq.org`) | **GNU LGPL v2.1+** | Modifications to Wine code must be published under LGPL. Dynamically linked user applications are not forced into LGPL, but Wine binaries must permit relinking. |
| **Box64 / Box86** | `ptitSeb/box64` | **MIT License** | Extremely permissive. Requires copyright notice and permission notice retention. |
| **DXVK** | `doitsujin/dxvk` | **Zlib / Libpng License** | Permissive open-source license. Modifications must not be misrepresented. |
| **VKD3D-Proton** | Hans-Kristian Arntzen | **GNU LGPL v2.1+** | Same terms as Wine. |
| **Mesa Turnip** | Mesa3D | **MIT License** | Permissive open-source license. |
| **PRoot** | PRoot Project | **GNU GPL v2.0** | Strong copyleft. Any bundled binary using PRoot in the container must comply with GPL v2. |

### Conclusion on Redistribution
Bundling precompiled binary blobs without explicit source tracking and attribution violates LGPL/GPL obligations. NexOS enforces clean architectural boundaries:
- The desktop shell, file manager, and PE inspector are written in clean Kotlin and released permissively.
- The runtime adapter connects to external container packages or user-provided rootfs installations.

---

## 3. Storage & Packaging Realities

A functional Wine 9.x prefix and glibc userland with Box64 requires:
- Minimal Ubuntu/Debian rootfs: **~80 MB** (compressed), **~220 MB** (uncompressed)
- Wine 9.x ARM64 + x86 runtime libraries: **~180 MB** (compressed), **~450 MB** (uncompressed)
- DXVK and Mesa drivers: **~40 MB**
- Default `$WINEPREFIX` virtual C: drive (`system32`, `syswow64`, registry hives): **~150 MB**
- **Total Initial Footprint**: **~450 MB to 1.1 GB**

### Packaging Recommendation
Standard Google Play / GitHub APK releases should remain under **50 MB** for fast installation and cloud CI builds. The heavy Wine/Box64 container should be distributed via:
1. An optional in-app container downloader (fetching verified SHA-256 rootfs tarballs).
2. Android App Bundle (AAB) Dynamic Feature Delivery.
3. User-imported `.tar.xz` rootfs packages stored in `context.filesDir/runtime/`.

---

## 4. Rendering and Window Integration Mechanism

Windows applications do not render natively via Android views or Jetpack Compose layout trees. They output frames through X11 or Wayland display sockets.

### How Windows Windows are Embedded into Jetpack Compose:
1. An internal lightweight X11 display server (such as an embedded XSDL or Xwayland instance) listens on local unix domain socket `abstract:@/tmp/.X11-unix/X0`.
2. Wine connects to `:0.0` using its `winex11.drv` driver.
3. The X server blits pixels directly into an Android `SurfaceView` or `TextureView` backed by hardware acceleration (`EGL` / `ANativeWindow`).
4. Inside Jetpack Compose, the `WindowFrame` composable hosts an `AndroidView(factory = { SurfaceView(it) })`.
5. Android touch gestures, mouse hover events, and physical keyboard `KeyEvents` are intercepted by the Compose window and dispatched across the socket to the X11 server, which injects them into Wine as Win32 `WM_MOUSEMOVE`, `WM_LBUTTONDOWN`, and `WM_KEYDOWN` messages.

---

## 5. Security Architecture

1. **Strict PE Inspection**:
   - `PeFileInspector` parses DOS headers, checks bounds on `e_lfanew`, verifies the `PE\0\0` signature, determines the COFF machine architecture, and calculates a SHA-256 checksum before execution.
2. **Container Isolation**:
   - The virtual Windows file system is mapped strictly to `context.filesDir/runtime/prefixes/default/drive_c`.
   - Windows applications have zero access to private Android app data outside the container, and no root permissions are ever granted.
3. **Honest Status Reporting**:
   - NexOS never fakes execution. If Wine or Box64 native binaries are not installed, the app explains the exact missing components and provides a clearly labeled Mock Simulator for testing.
