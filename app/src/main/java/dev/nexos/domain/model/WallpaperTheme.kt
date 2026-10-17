package dev.nexos.domain.model

enum class WallpaperTheme(val displayName: String, val description: String) {
    FLUENT_DARK("Fluent Obsidian", "Modern Windows dark gradient with subtle blue tones"),
    CYBER_NIGHT("Cyber Night", "Deep violet and slate angular flow"),
    EMERALD_AURORA("Emerald Aurora", "Deep forest green and cosmic teal gradient"),
    SOLAR_EMBER("Solar Ember", "Dark volcanic amber and charcoal warmth"),
    MINIMAL_CHARCOAL("Minimal Charcoal", "Distraction-free deep matte carbon"),
    CUSTOM_IMAGE("Custom Wallpaper", "User-selected image from device storage")
}
