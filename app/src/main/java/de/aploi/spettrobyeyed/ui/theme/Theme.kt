package de.aploi.spettrobyeyed.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val SpettroDarkColorScheme = darkColorScheme(
    primary = SpettroPrimaryDark,
    primaryContainer = SpettroPrimaryContainerDark,
    onPrimary = SpettroOnPrimary,
    onPrimaryContainer = SpettroPrimaryDark,
    secondary = SpettroSecondaryDark,
    secondaryContainer = SpettroSecondaryContainerDark,
    onSecondary = SpettroOnSecondary,
    onSecondaryContainer = SpettroSecondaryDark,
    tertiary = SpettroTertiaryDark,
    tertiaryContainer = SpettroTertiaryContainerDark,
    onTertiary = SpettroOnTertiary,
    onTertiaryContainer = SpettroTertiaryDark
)

private val SpettroLightColorScheme = lightColorScheme(
    primary = SpettroPrimary,
    primaryContainer = SpettroPrimaryContainer,
    onPrimary = SpettroOnPrimary,
    onPrimaryContainer = SpettroOnPrimaryContainer,
    secondary = SpettroSecondary,
    secondaryContainer = SpettroSecondaryContainer,
    onSecondary = SpettroOnSecondary,
    onSecondaryContainer = SpettroOnSecondaryContainer,
    tertiary = SpettroTertiary,
    tertiaryContainer = SpettroTertiaryContainer,
    onTertiary = SpettroOnTertiary,
    onTertiaryContainer = SpettroOnTertiaryContainer
)

@Composable
fun SpettroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SpettroDarkColorScheme
        else -> SpettroLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
