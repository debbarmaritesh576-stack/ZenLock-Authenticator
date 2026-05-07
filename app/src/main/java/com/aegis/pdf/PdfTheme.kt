package com.aegis.pdf  
  
import androidx.compose.material3.lightColorScheme  
import androidx.compose.ui.graphics.Color  
  
object PdfTheme {  
    private val AegisOrange = Color(0xFFFF5722)  
    private val DarkNavy = Color(0xFF1A1A2E)  
    private val LightGray = Color(0xFFF5F5F5)  
    private val White = Color(0xFFFFFFFF)  
    private val Black = Color(0xFF000000)  
  
    val colors = lightColorScheme(  
        primary = AegisOrange,  
        onPrimary = White,  
        secondary = DarkNavy,  
        onSecondary = White,  
        background = LightGray,  
        surface = White,  
        error = Color(0xFFD32F2F),  
        onBackground = Black,  
        onSurface = Black  
    )  
}