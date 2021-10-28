package se.warting.cities.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.BuildCompat

@Composable
fun CitiesTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {

    val context = LocalContext.current

    val colors = if (darkTheme && BuildCompat.isAtLeastS()) {
        dynamicDarkColorScheme(context) // Material you colors
    } else if (BuildCompat.isAtLeastS()) {
        dynamicLightColorScheme(context) // Material you colors
    } else if (darkTheme) {
        darkColorScheme() // Default colors
    } else {
        lightColorScheme() // Default colors
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}