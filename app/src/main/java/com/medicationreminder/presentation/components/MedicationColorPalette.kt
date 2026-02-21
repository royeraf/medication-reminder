package com.medicationreminder.presentation.components

import androidx.compose.ui.graphics.Color

/**
 * Predefined pill/medication colors for visual identification.
 */
object MedicationColors {
    val palette = listOf(
        Color(0xFF4F6AF5),  // Indigo
        Color(0xFF38BFA1),  // Teal
        Color(0xFFF5A623),  // Amber
        Color(0xFFE53935),  // Red
        Color(0xFF8E24AA),  // Purple
        Color(0xFF00897B),  // Dark Teal
        Color(0xFFD81B60),  // Pink
        Color(0xFF1E88E5),  // Blue
        Color(0xFF43A047),  // Green
        Color(0xFFFF7043),  // Deep Orange
        Color(0xFF6D4C41),  // Brown
        Color(0xFF546E7A),  // Blue Grey
    )

    fun getColor(index: Int): Color = palette[index % palette.size]

    /** Returns the color as an ARGB Int for use outside of Compose (e.g. notifications). */
    fun getColorArgb(index: Int): Int {
        val color = getColor(index)
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        return android.graphics.Color.rgb(r, g, b)
    }
}
