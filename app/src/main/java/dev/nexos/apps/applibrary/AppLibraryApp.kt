package dev.nexos.apps.applibrary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nexos.apps.AppDescriptor
import dev.nexos.apps.NexAppType
import dev.nexos.runtime.InstalledApp
import dev.nexos.ui.theme.EmeraldAccent
import dev.nexos.ui.theme.PurpleAccent
import dev.nexos.ui.theme.StatusError
import dev.nexos.ui.theme.StatusInfo
import dev.nexos.ui.theme.StatusSuccess
import dev.nexos.ui.theme.StatusWarning
import dev.nexos.ui.theme.TextMutedDark
import dev.nexos.ui.theme.TextPrimaryDark
import dev.nexos.ui.theme.TextSecondaryDark
import dev.nexos.ui.theme.WindowBorder
import dev.nexos.ui.theme.WindowDarkBg
import dev.nexos.ui.theme.WindowDarkTitleBar

@Composable
fun AppLibraryApp(
    viewModel: AppLibraryViewModel,
    onOpenBuiltIn: (NexAppType) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WindowDarkBg)
    ) {
        // Filter & Search Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WindowDarkTitleBar)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = state.selectedFilter == AppFilter.ALL,
                onClick = { viewModel.setFilter(AppFilter.ALL) },
                label = { Text("All (${state.builtInApps.size + state.windowsApps.size})", fontSize = 11.sp) },
                shape = RoundedCornerShape(4.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            FilterChip(
                selected = state.selectedFilter == AppFilter.BUILT_IN,
                onClick = { viewModel.setFilter(AppFilter.BUILT_IN) },
                label = { Text("Built-in (${state.builtInApps.size})", fontSize = 11.sp) },
                shape = RoundedCornerShape(4.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            FilterChip(
                selected = state.selectedFilter == AppFilter.WINDOWS_EXE,
                onClick = { viewModel.setFilter(AppFilter.WINDOWS_EXE) },
                label = { Text("Windows Executables (${state.windowsApps.size})", fontSize = 11.sp) },
                shape = RoundedCornerShape(4.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { viewModel.refresh() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextPrimaryDark, modifier = Modifier.size(16.dp))
            }
        }

        // Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(WindowDarkTitleBar)
                    .border(1.dp, WindowBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Search applications...", style = MaterialTheme.typography.bodySmall, color = TextMutedDark)
                }
            }
        }

        // Applications List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Show Built-in Apps
                    if (state.selectedFilter == AppFilter.ALL || state.selectedFilter == AppFilter.BUILT_IN) {
                        items(state.builtInApps) { app ->
                            BuiltInAppCard(
                                app = app,
                                onClick = { onOpenBuiltIn(app.type) }
                            )
                        }
                    }

                    // Show Windows Apps
                    if (state.selectedFilter == AppFilter.ALL || state.selectedFilter == AppFilter.WINDOWS_EXE) {
                        items(state.windowsApps) { winApp ->
                            WindowsAppCard(
                                app = winApp,
                                runtimeStatus = state.runtimeStatus,
                                isMock = state.isMock,
                                onLaunch = { viewModel.launchWindowsApp(winApp) {} },
                                onDelete = { viewModel.deleteWindowsApp(winApp) }
                            )
                        }
                    }
                }
            }
        }

        // Error Dialog
        state.errorMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissMessage() },
                title = { Text("Application Launch Notice", color = StatusWarning) },
                text = { Text(msg, color = TextPrimaryDark, style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    Button(onClick = { viewModel.dismissMessage() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun BuiltInAppCard(
    app: AppDescriptor,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = WindowDarkTitleBar),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = app.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(EmeraldAccent.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Native Compose", color = EmeraldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = app.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Launch, contentDescription = "Open", tint = TextPrimaryDark, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun WindowsAppCard(
    app: InstalledApp,
    runtimeStatus: dev.nexos.runtime.RuntimeStatus?,
    isMock: Boolean,
    onLaunch: () -> Unit,
    onDelete: () -> Unit
) {
    val canLaunch = runtimeStatus?.canLaunchRealWindowsBinaries == true || isMock

    Card(
        colors = CardDefaults.cardColors(containerColor = WindowDarkTitleBar),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = if (canLaunch) MaterialTheme.colorScheme.secondary else StatusWarning,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        val badgeColor = when {
                            isMock -> PurpleAccent
                            canLaunch -> StatusSuccess
                            else -> StatusWarning
                        }
                        val badgeText = when {
                            isMock -> "Mock Simulator"
                            canLaunch -> "Wine 9.x Ready"
                            else -> "Wine Container Missing"
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(badgeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(badgeText, color = badgeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${app.architecture} • ${app.formattedSize()} • ${app.executablePath}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedDark,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onLaunch,
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canLaunch) MaterialTheme.colorScheme.primary else StatusWarning.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(if (canLaunch) "Launch" else "Inspect", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}
