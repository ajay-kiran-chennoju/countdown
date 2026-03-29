package com.example.progresswidget

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import kotlin.math.ceil
import kotlin.math.min

class ProgressWidget : GlanceAppWidget() {
    
    override val sizeMode = SizeMode.Exact
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent()
        }
    }
}

@Composable
fun WidgetContent() {
    val context = LocalContext.current
    val size = LocalSize.current
    
    if (size.width.value <= 0 || size.height.value <= 0) return

    val density = context.resources.displayMetrics.density
    val widthPx = (size.width.value * density).toInt()
    val heightPx = (size.height.value * density).toInt()

    if (widthPx > 0 && heightPx > 0) {
        val bitmap = generateProgressBitmap(context, widthPx, heightPx)
        
        // Root layout - transparent, fully clickable, centers image
        Box(
            modifier = GlanceModifier.fillMaxSize()
                .clickable(actionStartActivity(android.content.ComponentName(context, MainActivity::class.java))),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = "Progress Grid",
                modifier = GlanceModifier.fillMaxSize()
            )
        }
    }
}

private fun generateProgressBitmap(context: Context, width: Int, height: Int): Bitmap {
    val bitmap = try {
        // By default, configuring ARGB_8888 will pre-fill with fully transparent pixels
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    } catch (e: Exception) {
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }
    
    val canvas = Canvas(bitmap)
    
    val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
    val totalDays = prefs.getInt("totalDays", 365)
    val currentDay = prefs.getInt("currentDay", 120)
    
    val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    val isDark = uiMode == Configuration.UI_MODE_NIGHT_YES
    
    // Fallback: adaptive light/dark theme matching Color A
    val defaultColorA = if (isDark) "#E0E0E0" else "#424242"
    val defaultColorB = "#FF5252"
    
    val colorAHex = prefs.getString("colorA", defaultColorA) ?: defaultColorA
    val colorBHex = prefs.getString("colorB", defaultColorB) ?: defaultColorB
    
    val colorA = parseColorSafely(colorAHex, Color.GRAY)
    val colorB = parseColorSafely(colorBHex, Color.RED)
    
    // Calculate perfect layout constraints
    var bestCols = 1
    var bestRows = 1
    var maxCellSize = 0f
    
    for (cols in 1..totalDays) {
        val rows = ceil(totalDays.toFloat() / cols).toInt()
        val cellSize = min(width.toFloat() / cols, height.toFloat() / rows)
        if (cellSize > maxCellSize) {
            maxCellSize = cellSize
            bestCols = cols
            bestRows = rows
        }
    }
    
    if (maxCellSize <= 0) return bitmap
    
    val gridWidth = bestCols * maxCellSize
    val gridHeight = bestRows * maxCellSize
    
    // Perfectly center grid inside widget bounds
    val startX = (width - gridWidth) / 2f
    val startY = (height - gridHeight) / 2f
    
    // 25% padding so dots have excellent spacing/flow without merging
    val padding = maxCellSize * 0.25f 
    val radius = (maxCellSize - padding * 2) / 2f
    
    val fillPaintA = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorA
        style = Paint.Style.FILL
    }
    
    val strokePaintA = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorA
        style = Paint.Style.STROKE
        strokeWidth = radius * 0.25f // Generous thickness for glanceability
    }
    
    val fillPaintB = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorB
        style = Paint.Style.FILL
    }
    
    for (i in 0 until totalDays) {
        val row = i / bestCols
        val col = i % bestCols
        
        val cx = startX + col * maxCellSize + maxCellSize / 2f
        val cy = startY + row * maxCellSize + maxCellSize / 2f
        
        when {
            i < currentDay -> {
                // Completed day -> fully filled A
                canvas.drawCircle(cx, cy, radius, fillPaintA)
            }
            i == currentDay -> {
                // Current day -> fully filled B
                canvas.drawCircle(cx, cy, radius, fillPaintB)
            }
            else -> {
                // Upcoming day -> stroke contour A
                // Shrinking radius so border doesn't break out of maximum circle bounds
                val r = radius - strokePaintA.strokeWidth / 2f
                if (r > 0) {
                    canvas.drawCircle(cx, cy, r, strokePaintA)
                }
            }
        }
    }
    
    return bitmap
}

private fun parseColorSafely(hex: String, fallback: Int): Int {
    return try {
        Color.parseColor(if (hex.startsWith("#")) hex else "#$hex")
    } catch (e: Exception) {
        fallback
    }
}
