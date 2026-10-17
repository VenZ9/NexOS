package dev.nexos.apps.filemanager

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nexos.domain.model.NexFile
import dev.nexos.ui.theme.StatusInfo
import dev.nexos.ui.theme.StatusWarning
import dev.nexos.ui.theme.TextMutedDark
import dev.nexos.ui.theme.TextPrimaryDark
import dev.nexos.ui.theme.TextSecondaryDark
import dev.nexos.ui.theme.WindowBorder
import dev.nexos.ui.theme.WindowDarkBg
import dev.nexos.ui.theme.WindowDarkTitleBar
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FileManagerApp(
    viewModel: FileManagerViewModel,
    onOpenFile: (NexFile) -> Unit,
    onLaunchExe: (NexFile) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }

    // SAF File Picker for importing .exe / .msi or general files
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromUri(it) }
    }

    // SAF Directory Picker for browsing host external directories
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        // Scoped SAF folder access
        uri?.let {
            viewModel.importFromUri(it)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WindowDarkBg)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WindowDarkTitleBar)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateBack() },
                enabled = state.canGoBack,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = if (state.canGoBack) TextPrimaryDark else TextMutedDark,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = { viewModel.navigateUp() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Up",
                    tint = TextPrimaryDark,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Current Path / Breadcrumb Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(WindowDarkBg)
                    .border(1.dp, WindowBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val displayPath = state.currentPath.substringAfter("NexOS_Drive", "NexOS:\\")
                Text(
                    text = "NexOS:\\" + displayPath.removePrefix("/"),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action Buttons
            Button(
                onClick = { importLauncher.launch(arrayOf("*/*")) },
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Import .exe", fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = {
                    newNameInput = ""
                    showNewFileDialog = true
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Document", tint = TextPrimaryDark, modifier = Modifier.size(16.dp))
            }

            IconButton(
                onClick = {
                    newNameInput = ""
                    showNewFolderDialog = true
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = TextPrimaryDark, modifier = Modifier.size(16.dp))
            }

            if (state.selectedItem != null) {
                IconButton(
                    onClick = { viewModel.deleteSelectedItem() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }
            }

            IconButton(
                onClick = { viewModel.toggleViewMode() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (state.isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = "Toggle View",
                    tint = TextPrimaryDark,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Subheader: Quick Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
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
                    if (state.searchQuery.isEmpty()) {
                        Text("Search current directory...", style = MaterialTheme.typography.bodySmall, color = TextMutedDark)
                    }
                }
            }
        }

        // Main Content Area with Navigation Sidebar + File View
        Row(modifier = Modifier.weight(1f)) {
            // Quick Access Left Sidebar
            Column(
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .background(WindowDarkTitleBar.copy(alpha = 0.5f))
                    .border(1.dp, WindowBorder.copy(alpha = 0.5f))
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = "QUICK ACCESS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedDark,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )

                QuickAccessItem(
                    title = "Virtual (C:)",
                    icon = Icons.Default.Laptop,
                    onClick = {
                        val cDir = File(state.currentPath.substringBefore("NexOS_Drive") + "NexOS_Drive/C")
                        viewModel.loadDirectory(cDir)
                    }
                )

                QuickAccessItem(
                    title = "Documents",
                    icon = Icons.Default.Folder,
                    onClick = {
                        val docsDir = File(state.currentPath.substringBefore("NexOS_Drive") + "NexOS_Drive/Documents")
                        viewModel.loadDirectory(docsDir)
                    }
                )

                QuickAccessItem(
                    title = "Installers",
                    icon = Icons.Default.PlayCircle,
                    onClick = {
                        val instDir = File(state.currentPath.substringBefore("NexOS_Drive") + "NexOS_Drive/Installers")
                        viewModel.loadDirectory(instDir)
                    }
                )

                QuickAccessItem(
                    title = "Downloads",
                    icon = Icons.Default.Folder,
                    onClick = {
                        val dlDir = File(state.currentPath.substringBefore("NexOS_Drive") + "NexOS_Drive/Downloads")
                        viewModel.loadDirectory(dlDir)
                    }
                )
            }

            // File Listing Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
            ) {
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
                    }
                } else if (state.filteredItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (state.searchQuery.isNotEmpty()) "No files match '${state.searchQuery}'" else "Folder is empty",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMutedDark
                        )
                    }
                } else if (state.isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 88.dp),
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.filteredItems, key = { it.id }) { file ->
                            FileGridItem(
                                file = file,
                                isSelected = state.selectedItem?.id == file.id,
                                onClick = { viewModel.selectItem(file) },
                                onDoubleClick = {
                                    if (file.isDirectory) {
                                        viewModel.navigateInto(file)
                                    } else if (file.isWindowsExe) {
                                        onLaunchExe(file)
                                    } else {
                                        onOpenFile(file)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.filteredItems, key = { it.id }) { file ->
                            FileListItem(
                                file = file,
                                isSelected = state.selectedItem?.id == file.id,
                                onClick = { viewModel.selectItem(file) },
                                onDoubleClick = {
                                    if (file.isDirectory) {
                                        viewModel.navigateInto(file)
                                    } else if (file.isWindowsExe) {
                                        onLaunchExe(file)
                                    } else {
                                        onOpenFile(file)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Status Bar at Bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(WindowDarkTitleBar)
                .border(1.dp, WindowBorder.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.filteredItems.size} items",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            val selected = state.selectedItem
            if (selected != null) {
                Text(
                    text = "${selected.name} (${selected.formattedSize()})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (selected.isWindowsExe && state.inspectedPe != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "[PE: ${state.inspectedPe?.machineName ?: "Windows"}]",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusInfo,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    // Dialog: New Folder
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = newNameInput,
                    onValueChange = { newNameInput = it },
                    label = { Text("Folder Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNameInput.isNotBlank()) {
                            viewModel.createFolder(newNameInput.trim())
                            showNewFolderDialog = false
                        }
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog: New Text File
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("New Text Document") },
            text = {
                OutlinedTextField(
                    value = newNameInput,
                    onValueChange = { newNameInput = it },
                    label = { Text("Document Name (e.g. notes.txt)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNameInput.isNotBlank()) {
                            viewModel.createNewDocument(newNameInput.trim()) { createdFile ->
                                onOpenFile(
                                    NexFile(
                                        id = createdFile.absolutePath,
                                        name = createdFile.name,
                                        path = createdFile.absolutePath
                                    )
                                )
                            }
                            showNewFileDialog = false
                        }
                    }
                ) { Text("Create & Open") }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun QuickAccessItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.bodySmall, color = TextPrimaryDark, maxLines = 1)
    }
}

@Composable
private fun FileListItem(
    file: NexFile,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
    val dateFormat = remember { SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .clickable(onClick = {
                onClick()
                onDoubleClick()
            })
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = getFileIcon(file),
            contentDescription = null,
            tint = getFileIconTint(file),
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = file.name,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimaryDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = dateFormat.format(Date(file.lastModified)),
            style = MaterialTheme.typography.bodySmall,
            color = TextMutedDark,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = file.formattedSize(),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryDark,
            fontSize = 11.sp,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
private fun FileGridItem(
    file: NexFile,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent

    Column(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(onClick = {
                onClick()
                onDoubleClick()
            })
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = getFileIcon(file),
            contentDescription = null,
            tint = getFileIconTint(file),
            modifier = Modifier.size(36.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = file.name,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimaryDark,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun getFileIcon(file: NexFile): ImageVector {
    return when {
        file.isDirectory -> Icons.Default.Folder
        file.isWindowsExe -> Icons.Default.PlayCircle
        file.isTextDocument -> Icons.Default.Description
        file.isImage -> Icons.Default.Image
        else -> Icons.Default.Description
    }
}

private fun getFileIconTint(file: NexFile): Color {
    return when {
        file.isDirectory -> Color(0xFFF59E0B)
        file.isWindowsExe -> Color(0xFF60CDFF)
        file.isTextDocument -> Color(0xFF93C5FD)
        file.isImage -> Color(0xFFA78BFA)
        else -> Color(0xFFA1A1AA)
    }
}
