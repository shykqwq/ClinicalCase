package com.clinicalcase.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F6FEB),
    secondary = Color(0xFF4F6F8F),
    tertiary = Color(0xFF00796B),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
)

@Composable
fun ClinicalCaseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}

