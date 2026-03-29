package com.example.progresswidget

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
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
    
    // Allocate exactly 24dp strictly for text space at bottom, adjusting the grid size dynamically
    val textSpacePx = (24 * density).toInt() 
    
    val widthPx = (size.width.value * density).toInt()
    val availableHeightPx = (size.height.value * density).toInt() - textSpacePx

    val intent = Intent(context, MainActivity::class.java).apply {
        putExtra("fromWidget", true)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    
    // Check Date Logic
    val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
    val now = LocalDate.now(ZoneId.systemDefault())
    
    // Evaluate timestamps mapped directly to Midnight exact dates
    val startMillis = prefs.getLong("startDate", Instant.now().toEpochMilli())
    val endMillis = prefs.getLong("endDate", Instant.now().plus(365, ChronoUnit.DAYS).toEpochMilli())
    
    val start = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val end = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()

    val totalDays = ChronoUnit.DAYS.between(start, end).toInt().coerceAtLeast(1) // Safety constraint
    val unboundedCurrentDay = ChronoUnit.DAYS.between(start, now).toInt()
    val currentDay = unboundedCurrentDay.coerceIn(0, totalDays) // Handle future start bounds
    
    // Theme resolution for native visual aesthetics
    val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    val isDark = uiMode == Configuration.UI_MODE_NIGHT_YES
    val textColor = if (isDark) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.DarkGray

    Column(
        modifier = GlanceModifier.fillMaxSize()
            .clickable(actionStartActivity(intent)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
            contentAlignment = Alignment.Center
        ) {
            if (widthPx > 0 && availableHeightPx > 0) {
                // Generates accurate mathematical visual mapping matrix dynamically mapping to exact days
                val bitmap = generateProgressBitmap(widthPx, availableHeightPx, totalDays, currentDay, prefs, isDark)
                Image(
                    provider = ImageProvider(bitmap),
                    contentDescription = "Progress Grid",
                    modifier = GlanceModifier.fillMaxSize()
                )
            }
        }
        
        Text(
            text = "Day $currentDay / $totalDays",
            style = TextStyle(
                color = ColorProvider(textColor),
                fontWeight = FontWeight.Medium
            ),
            modifier = GlanceModifier.padding(bottom = 4.dp)
        )
    }
}

private fun generateProgressBitmap(
    width: Int, height: Int, 
    totalDays: Int, currentDay: Int, 
    prefs: SharedPreferences, isDark: Boolean
): Bitmap {
    val bitmap = try {
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    } catch (e: Exception) {
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }
    
    val canvas = Canvas(bitmap)
    
    val defaultColorA = if (isDark) "#E0E0E0" else "#424242"
    val defaultColorB = "#FF5252"
    
    val colorA = parseColorSafely(prefs.getString("colorA", defaultColorA) ?: defaultColorA, AndroidColor.GRAY)
    val colorB = parseColorSafely(prefs.getString("colorB", defaultColorB) ?: defaultColorB, AndroidColor.RED)
    
    // Calculation targeting symmetrical grids scaled proportionally without edge overflows 
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
    
    val startX = (width - gridWidth) / 2f
    val startY = (height - gridHeight) / 2f
    
    val padding = maxCellSize * 0.25f 
    val radius = (maxCellSize - padding * 2) / 2f
    
    val fillPaintA = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorA; style = Paint.Style.FILL }
    val strokePaintA = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
        color = colorA; style = Paint.Style.STROKE; strokeWidth = radius * 0.25f 
    }
    val fillPaintB = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorB; style = Paint.Style.FILL }
    
    for (i in 0 until totalDays) {
        val row = i / bestCols
        val col = i % bestCols
        
        val cx = startX + col * maxCellSize + maxCellSize / 2f
        val cy = startY + row * maxCellSize + maxCellSize / 2f
        
        when {
            i < currentDay -> canvas.drawCircle(cx, cy, radius, fillPaintA)
            i == currentDay -> canvas.drawCircle(cx, cy, radius, fillPaintB)
            else -> {
                val r = radius - strokePaintA.strokeWidth / 2f
                if (r > 0) canvas.drawCircle(cx, cy, r, strokePaintA)
            }
        }
    }
    
    return bitmap
}

private fun parseColorSafely(hex: String, fallback: Int): Int {
    return try {
        AndroidColor.parseColor(if (hex.startsWith("#")) hex else "#$hex")
    } catch (e: Exception) {
        fallback
    }
}
