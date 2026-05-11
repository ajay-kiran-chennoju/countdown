package com.example.progresswidget.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progresswidget.model.BatteryEvent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryTrackerScreen(
    viewModel: BatteryTrackerViewModel = viewModel(),
    onBack: () -> Unit
) {
    val events by viewModel.events.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battery Tracker") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ExpandMore, contentDescription = "Back", modifier = Modifier.rotate(90f))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(events, key = { it.id }) { event ->
                AutomationCard(
                    event = event,
                    onUpdate = { enabled, text -> viewModel.updateEvent(event.id, enabled, text) },
                    onTest = { viewModel.testVoice(it) }
                )
            }
        }
    }
}

@Composable
fun AutomationCard(
    event: BatteryEvent,
    onUpdate: (Boolean, String) -> Unit,
    onTest: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var ttsText by remember(event.ttsText) { mutableStateOf(event.ttsText) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (event.isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (event.lastTriggered > 0) {
                        val date = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(event.lastTriggered))
                        Text(
                            text = "Last triggered: $date",
                            style = MaterialTheme.typography.labelSmall
                        )
                    } else {
                        Text(
                            text = "Status: ${if (event.isEnabled) "Active" else "Disabled"}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Switch(
                    checked = event.isEnabled,
                    onCheckedChange = { onUpdate(it, ttsText) }
                )

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Trigger: ${event.description}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = ttsText,
                        onValueChange = { ttsText = it },
                        label = { Text("TTS Announcement") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { onTest(ttsText) },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Voice")
                        }

                        Button(
                            onClick = { onUpdate(event.isEnabled, ttsText) }
                        ) {
                            Text("Save Settings")
                        }
                    }
                }
            }
        }
    }
}
