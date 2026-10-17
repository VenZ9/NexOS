package dev.nexos.desktop.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import dev.nexos.domain.model.WallpaperTheme
import dev.nexos.ui.theme.DesktopDarkBg
import java.io.InputStream

@Composable
fun WallpaperView(
    theme: WallpaperTheme,
    customUriString: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (theme == WallpaperTheme.CUSTOM_IMAGE && customUriString.isNotBlank()) {
        val bitmap = remember(customUriString) {
            try {
                val uri = Uri.parse(customUriString)
                val stream: InputStream? = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Desktop Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize()
            )
            return
        }
    }

    // Procedural Dark Gradients
    val brush = when (theme) {
        WallpaperTheme.FLUENT_DARK -> Brush.radialGradient(
            colors = listOf(
                Color(0xFF1E293B),
                Color(0xFF0F172A),
                Color(0xFF090D16)
            ),
            center = Offset(800f, 400f),
            radius = 1200f
        )
        WallpaperTheme.CYBER_NIGHT -> Brush.linearGradient(
            colors = listOf(
                Color(0xFF1A102F),
                Color(0xFF0E0B1A),
                Color(0xFF050508)
            ),
            start = Offset(0f, 0f),
            end = Offset(1200f, 1000f)
        )
        WallpaperTheme.EMERALD_AURORA -> Brush.linearGradient(
            colors = listOf(
                Color(0xFF06281E),
                Color(0xFF0A1F18),
                Color(0xFF040B08)
            ),
            start = Offset(200f, 0f),
            end = Offset(1000f, 1000f)
        )
        WallpaperTheme.SOLAR_EMBER -> Brush.radialGradient(
            colors = listOf(
                Color(0xFF2E150B),
                Color(0xFF190C05),
                Color(0xFF0B0603)
            ),
            center = Offset(600f, 600f),
            radius = 1000f
        )
        WallpaperTheme.MINIMAL_CHARCOAL, WallpaperTheme.CUSTOM_IMAGE -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF18181B),
                Color(0xFF121214)
            )
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(brush = brush)
    }
}
