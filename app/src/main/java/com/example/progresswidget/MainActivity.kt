package com.example.progresswidget

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Single Activity logic intercepting widget vs manual launch params seamlessly
        val launchedFromWidget = intent?.getBooleanExtra("fromWidget", false) ?: false
        
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var showConfigScreen by remember { mutableStateOf(launchedFromWidget) }
                    val context = androidx.compose.ui.platform.LocalContext.current
                    
                    if (showConfigScreen) {
                        ConfigScreen(context)
                    } else {
                        LauncherScreen { showConfigScreen = true }
                    }
                }
            }
        }
    }
}

@Composable
fun LauncherScreen(onNavigate: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onNavigate) {
            Text("Configure tracker", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun ConfigScreen(context: Context) {
    val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
    
    val defaultStart = Instant.now().toEpochMilli()
    val defaultEnd = Instant.now().plus(java.time.Duration.ofDays(365)).toEpochMilli()
    
    var startMillis by remember { mutableStateOf(prefs.getLong("startDate", defaultStart)) }
    var endMillis by remember { mutableStateOf(prefs.getLong("endDate", defaultEnd)) }
    
    var colorA by remember { mutableStateOf(prefs.getString("colorA", "#E0E0E0") ?: "#E0E0E0") }
    var colorB by remember { mutableStateOf(prefs.getString("colorB", "#FF5252") ?: "#FF5252") }
    
    val coroutineScope = rememberCoroutineScope()
    
    val format = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val startDateStr = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate().format(format)
    val endDateStr = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate().format(format)

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Tracker Configuration", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        Card(
            modifier = Modifier.fillMaxWidth().clickable {
                showDatePickerDialog(context, startMillis) { startMillis = it }
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Start Date", style = MaterialTheme.typography.labelMedium)
                Text(startDateStr, style = MaterialTheme.typography.bodyLarge)
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth().clickable {
                showDatePickerDialog(context, endMillis) { endMillis = it }
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("End Date", style = MaterialTheme.typography.labelMedium)
                Text(endDateStr, style = MaterialTheme.typography.bodyLarge)
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = colorA,
                onValueChange = { colorA = it },
                label = { Text("Color A (HEX)") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(matcherHex(colorA))
        }
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = colorB,
                onValueChange = { colorB = it },
                label = { Text("Color B (HEX)") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(matcherHex(colorB))
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = {
                prefs.edit()
                    .putLong("startDate", startMillis)
                    .putLong("endDate", endMillis)
                    .putString("colorA", colorA)
                    .putString("colorB", colorB)
                    .apply()
                    
                coroutineScope.launch {
                    ProgressWidget().updateAll(context)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Save & Apply")
        }
    }
}

@Composable
fun matcherHex(hex: String): Modifier {
    val colorInt = try {
        android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex")
    } catch (e: Exception) {
        android.graphics.Color.TRANSPARENT
    }
    return Modifier.size(48.dp)
        .clip(CircleShape)
        .background(Color(colorInt))
}

fun showDatePickerDialog(context: Context, initMillis: Long, onDateSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initMillis }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val sel = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateSelected(sel.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
