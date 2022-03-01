package io.github.vincentvibe3.pixivdownloader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = primaryDark,
    primaryVariant = primaryAltDark,
    secondary = secondaryDark,
    surface = CardBackgroundDark,
    background = black,
    onPrimary = white,
    onSecondary = black,
    onBackground = white,
    onSurface = white,
)

private val LightColorPalette = lightColors(
    primary = primaryLight,
    primaryVariant = primaryAltLight,
    secondary = secondaryLight,
    surface = CardBackgroundLight,
            //Other default colors to override
    background = white,
    onPrimary = white,
    onSecondary = black,
    onBackground = black,
    onSurface = black,
    //
)

@Composable
fun PixivDownloaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}