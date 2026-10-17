package dev.nexos.apps.texteditor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nexos.ui.theme.StatusSuccess
import dev.nexos.ui.theme.TextMutedDark
import dev.nexos.ui.theme.TextPrimaryDark
import dev.nexos.ui.theme.TextSecondaryDark
import dev.nexos.ui.theme.WindowBorder
import dev.nexos.ui.theme.WindowDarkBg
import dev.nexos.ui.theme.WindowDarkTitleBar
import java.io.File

@Composable
fun TextEditorApp(
    viewModel: TextEditorViewModel,
    initialFilePath: String? = null,
    onTitleChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(initialFilePath) {
        if (!initialFilePath.isNullOrBlank()) {
            val file = File(initialFilePath)
            if (file.exists()) {
                viewModel.loadFromFile(file)
            }
        }
    }

    LaunchedEffect(state.fileName, state.isModified) {
        val title = if (state.isModified) "*${state.fileName}" else state.fileName
        onTitleChanged(title)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WindowDarkBg)
    ) {
        // Classic Notepad Menu Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WindowDarkTitleBar)
                .border(1.dp, WindowBorder.copy(alpha = 0.5f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { viewModel.resetNewDocument() },
                modifier = Modifier.height(28.dp)
            ) {
                Text("New", color = TextPrimaryDark, fontSize = 11.sp)
            }

            TextButton(
                onClick = {
                    viewModel.save { savedName ->
                        onTitleChanged(savedName)
                    }
                },
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save", color = TextPrimaryDark, fontSize = 11.sp)
            }

            TextButton(
                onClick = { viewModel.toggleWordWrap() },
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.WrapText, contentDescription = null, tint = if (state.wordWrap) MaterialTheme.colorScheme.primary else TextMutedDark, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Wrap: ${if (state.wordWrap) "On" else "Off"}", color = TextPrimaryDark, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            if (state.isModified) {
                Text(
                    text = "• Unsaved changes",
                    color = Color(0xFFF59E0B),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        // Text Editing Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            BasicTextField(
                value = state.content,
                onValueChange = { viewModel.updateContent(it) },
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = TextPrimaryDark,
                    lineHeight = 18.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxSize()
            )

            if (state.content.isEmpty()) {
                Text(
                    text = "Type text here or open a file...",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = TextMutedDark
                    )
                )
            }
        }

        // Notepad Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .background(WindowDarkTitleBar)
                .border(1.dp, WindowBorder.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.statusMessage ?: "Ready",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark,
                fontSize = 10.sp,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Lines: ${state.lineCount}  Chars: ${state.charCount}",
                style = MaterialTheme.typography.bodySmall,
                color = TextMutedDark,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "UTF-8",
                style = MaterialTheme.typography.bodySmall,
                color = TextMutedDark,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Windows (CRLF)",
                style = MaterialTheme.typography.bodySmall,
                color = TextMutedDark,
                fontSize = 10.sp
            )
        }
    }
}
