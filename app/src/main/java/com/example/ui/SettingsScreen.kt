package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import com.example.data.remote.model.SenderRule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = when(uiState.themeMode) { 1 -> false; 2 -> true; else -> isSystemInDarkTheme() }
    val cardBackground = MaterialTheme.colorScheme.surface
    val tagBackground = MaterialTheme.colorScheme.secondaryContainer
    val successColor = if (isDark) SuccessGreenDark else SuccessGreen
    val successTextColor = if (isDark) SuccessGreenTextDark else SuccessGreenText

    var showAddRuleDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SMS Connector",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(
                onClick = { viewModel.setThemeMode(if (isDark) 1 else 2) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = if (isDark) "☀️" else "🌙",
                    fontSize = 18.sp
                )
            }
        }

        // Main Content Area (Flexible)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(28.dp))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "SERVICE STATUS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(successColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Monitoring Active",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.5f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "WORKER V1.0",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Last Sync",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = "Just now",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "JWT Status",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = if (uiState.jwtToken.isNullOrEmpty()) "INVALID" else "VALID",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.jwtToken.isNullOrEmpty()) MaterialTheme.colorScheme.error else successTextColor
                            )
                        }
                    }
                }
            }

            // Statistics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pending Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(cardBackground, RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Pending",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "${uiState.pendingCount}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Total Uploaded Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(cardBackground, RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Uploaded",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "${uiState.syncedCount}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Dead Letter Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(cardBackground, RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Dead Letter",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "${uiState.deadLetterCount}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Whitelist Configuration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBackground, RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                    .padding(20.dp)
                    .weight(1f, fill = false)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sender Whitelist",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${uiState.rules.size} ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { showAddRuleDialog = true },
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Rule",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    if (uiState.rules.isEmpty()) {
                        Text(
                            text = "No sender rules configured.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.rules.forEach { rule ->
                               Row(
                                    modifier = Modifier
                                        .background(tagBackground, CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${rule.type}: ${rule.value}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { viewModel.removeSenderRule(rule) },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Rule",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Device Info Mini Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DEVICE ID: ${uiState.deviceId?.take(18) ?: "LOADING..."}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(
                    text = "V1.0.0-RELEASE",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        // Bottom Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.syncNow() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Sync Now", fontWeight = FontWeight.Medium)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.refreshWhitelist() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                ) {
                    Text(text = "Refresh Senders", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                
                if (uiState.jwtToken.isNullOrEmpty()) {
                    OutlinedButton(
                        onClick = { viewModel.login("DEMO_JWT_TOKEN_123") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "Mock Login", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text(text = "Logout", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    if (showAddRuleDialog) {
        AddRuleDialog(
            onDismiss = { showAddRuleDialog = false },
            onAdd = { rule -> 
                viewModel.addSenderRule(rule)
                showAddRuleDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRuleDialog(
    onDismiss: () -> Unit,
    onAdd: (SenderRule) -> Unit
) {
    var type by remember { mutableStateOf("EXACT") }
    var value by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Sender Rule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Match Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("EXACT", "PREFIX", "REGEX").forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    type = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Sender Value") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (value.isNotBlank()) {
                        onAdd(SenderRule(type = type, value = value.trim()))
                    }
                },
                enabled = value.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

