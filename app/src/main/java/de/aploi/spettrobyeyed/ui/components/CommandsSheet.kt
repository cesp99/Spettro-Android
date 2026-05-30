package de.aploi.spettrobyeyed.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class CommandItem(
    val command: String,
    val description: String,
    val icon: ImageVector,
    val category: String
)

private val allCommands = listOf(
    // Agents & Modes
    CommandItem("/plan", "Switch to planning mode or run a plan", Icons.Rounded.Psychology, "Agents & Modes"),
    CommandItem("/approve", "Execute the approved plan via coding agent", Icons.Rounded.Bolt, "Agents & Modes"),
    CommandItem("/mode", "Cycle to the next agent mode", Icons.AutoMirrored.Rounded.Send, "Agents & Modes"),
    CommandItem("/next", "Alias for /mode", Icons.AutoMirrored.Rounded.Send, "Agents & Modes"),

    // Session
    CommandItem("/clear", "Save and clear conversation", Icons.Rounded.Folder, "Session"),
    CommandItem("/resume", "Load a previous saved conversation", Icons.Rounded.Folder, "Session"),
    CommandItem("/compact", "Summarize conversation to save tokens", Icons.Rounded.Memory, "Session"),
    CommandItem("/compact auto on", "Enable automatic compaction", Icons.Rounded.Memory, "Session"),
    CommandItem("/compact auto off", "Disable automatic compaction", Icons.Rounded.Memory, "Session"),
    CommandItem("/tasks", "Show current task list", Icons.Rounded.Folder, "Session"),
    CommandItem("/tasks add", "Add a new task", Icons.Rounded.Folder, "Session"),

    // Model & Providers
    CommandItem("/connect", "Open provider & local endpoint setup", Icons.Rounded.Router, "Model & Providers"),
    CommandItem("/models", "Open model selector", Icons.Rounded.Code, "Model & Providers"),
    CommandItem("/thinking off", "Disable extended thinking", Icons.Rounded.Psychology, "Model & Providers"),
    CommandItem("/thinking low", "Low thinking budget", Icons.Rounded.Psychology, "Model & Providers"),
    CommandItem("/thinking medium", "Medium thinking budget", Icons.Rounded.Psychology, "Model & Providers"),
    CommandItem("/thinking high", "High thinking budget", Icons.Rounded.Psychology, "Model & Providers"),
    CommandItem("/thinking x-high", "Extra-high thinking budget", Icons.Rounded.Psychology, "Model & Providers"),
    CommandItem("/budget 0", "Unlimited token budget", Icons.Rounded.Memory, "Model & Providers"),

    // Permissions
    CommandItem("/permission yolo", "Allow all actions without approval", Icons.Rounded.Bolt, "Permissions"),
    CommandItem("/permission ask-first", "Ask for approval before risky actions", Icons.Rounded.Security, "Permissions"),
    CommandItem("/permission restricted", "Read-only mode, no writes or executes", Icons.Rounded.Security, "Permissions"),
    CommandItem("/permissions debug on", "Show permission debug info", Icons.Rounded.Security, "Permissions"),
    CommandItem("/permissions debug off", "Hide permission debug info", Icons.Rounded.Security, "Permissions"),

    // Project
    CommandItem("/init", "Analyze repo and create SPETTRO.md", Icons.Rounded.Folder, "Project"),
    CommandItem("/hooks", "Show effective runtime hooks", Icons.Rounded.Bolt, "Project"),
    CommandItem("/mcp list", "List MCP resources", Icons.Rounded.Extension, "Project"),

    // Remote
    CommandItem("/remote status", "Show current remote URL and token", Icons.Rounded.Router, "Remote"),
    CommandItem("/remote stop", "Stop the remote control server", Icons.Rounded.Router, "Remote"),

    // Skills
    CommandItem("/skill list", "List all installed skills", Icons.Rounded.Extension, "Skills"),
    CommandItem("/skill install", "Install a skill from path or git", Icons.Rounded.Extension, "Skills"),
    CommandItem("/skill enable", "Enable a specific skill", Icons.Rounded.Extension, "Skills"),
    CommandItem("/skill disable", "Disable a specific skill", Icons.Rounded.Extension, "Skills"),
    CommandItem("/skill info", "Show metadata for a skill", Icons.Rounded.Extension, "Skills"),
    CommandItem("/skill where", "Show skill discovery locations", Icons.Rounded.Extension, "Skills"),

    // Telegram
    CommandItem("/telegram status", "Show Telegram relay status", Icons.AutoMirrored.Rounded.Message, "Telegram"),
    CommandItem("/telegram start", "Start Telegram relay worker", Icons.AutoMirrored.Rounded.Message, "Telegram"),
    CommandItem("/telegram stop", "Stop Telegram relay worker", Icons.AutoMirrored.Rounded.Message, "Telegram"),
    CommandItem("/telegram list", "List allowlist entries", Icons.AutoMirrored.Rounded.Message, "Telegram"),

    // Help
    CommandItem("/help", "Show help text", Icons.AutoMirrored.Rounded.Help, "Help")
)

private val categoryIcons = mapOf(
    "Agents & Modes" to Icons.Rounded.Psychology,
    "Session" to Icons.Rounded.Folder,
    "Model & Providers" to Icons.Rounded.Code,
    "Permissions" to Icons.Rounded.Security,
    "Project" to Icons.Rounded.Folder,
    "Remote" to Icons.Rounded.Router,
    "Skills" to Icons.Rounded.Extension,
    "Telegram" to Icons.AutoMirrored.Rounded.Message,
    "Help" to Icons.AutoMirrored.Rounded.Help
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandsSheet(
    onDismiss: () -> Unit,
    onCommandSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var searchQuery by remember { mutableStateOf("") }

    val filteredCommands = remember(searchQuery) {
        if (searchQuery.isBlank()) allCommands
        else allCommands.filter { cmd ->
            cmd.command.contains(searchQuery, ignoreCase = true) ||
                cmd.description.contains(searchQuery, ignoreCase = true) ||
                cmd.category.contains(searchQuery, ignoreCase = true)
        }
    }

    val groupedCommands = remember(filteredCommands) {
        filteredCommands.groupBy { it.category }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Text(
                "Commands",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search commands…") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                groupedCommands.forEach { (category, commands) ->
                    item(key = "header_$category") {
                        CategoryHeader(category, categoryIcons[category] ?: Icons.Rounded.Bolt)
                    }
                    items(commands, key = { it.command }) { cmd ->
                        CommandRow(cmd) { onCommandSelected(cmd.command) }
                    }
                    item(key = "divider_$category") {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun CommandRow(cmd: CommandItem, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                cmd.icon, null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cmd.command,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = cmd.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
