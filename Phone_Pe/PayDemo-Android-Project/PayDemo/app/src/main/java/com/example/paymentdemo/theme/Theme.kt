package com.example.paymentdemo.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Scheme = lightColorScheme(
    primary = Color(0xFF5F259F), onPrimary = Color.White,
    secondary = Color(0xFF673AB7), background = Color(0xFFF8F4FC),
    surface = Color.White, onSurface = Color(0xFF212121),
    error = Color(0xFFD93025)
)

@Composable
fun PayDemoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = Typography(), content = content)
}
