package de.aploi.spettrobyeyed.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.aploi.spettrobyeyed.ui.components.CommandsSheet
import de.aploi.spettrobyeyed.ui.components.EventItem
import de.aploi.spettrobyeyed.ui.viewmodel.SpettroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: SpettroViewModel,
    onDisconnect: () -> Unit
) {
    val events by viewModel.events.collectAsState()
    val status by viewModel.status.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    var showCommandsSheet by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val atBottom by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= listState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(events.size) {
        if (atBottom && events.isNotEmpty()) {
            listState.animateScrollToItem(events.size - 1)
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Spettro",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (status.mode.isNotEmpty()) {
                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            status.mode,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    modifier = Modifier.height(22.dp),
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDisconnect) {
                            Icon(
                                Icons.Rounded.PowerSettingsNew,
                                "Disconnect",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    actions = {
                        AnimatedVisibility(visible = status.thinking) {
                            IconButton(onClick = { viewModel.interrupt() }) {
                                Icon(
                                    Icons.Rounded.StopCircle,
                                    "Stop",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        if (status.tokensUsed > 0) {
                            Text(
                                "${status.tokensUsed / 1000}k",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }

                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(Icons.Rounded.MoreVert, "More")
                            }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Clear events") },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.sendMessage("/clear")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Compact conversation") },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.sendMessage("/compact")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Disconnect") },
                                    onClick = {
                                        showMoreMenu = false
                                        onDisconnect()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.PowerSettingsNew, null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )

                AnimatedVisibility(
                    visible = status.thinking,
                    enter = slideInVertically() + fadeIn(),
                    exit = fadeOut()
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        bottomBar = {
            MessageInputBar(
                text = inputText,
                onTextChange = { viewModel.setInputText(it) },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText.trim())
                        viewModel.setInputText("")
                    }
                },
                onCommandsClick = { showCommandsSheet = true },
                hasApprovalPending = events.any {
                    it is de.aploi.spettrobyeyed.data.models.SpettroEvent.ApprovalRequest ||
                        it is de.aploi.spettrobyeyed.data.models.SpettroEvent.AskUser
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (events.isEmpty()) {
            EmptyState(modifier = Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(events) { _, event ->
                    EventItem(event = event)
                }
            }
        }
    }

    if (showCommandsSheet) {
        CommandsSheet(
            onDismiss = { showCommandsSheet = false },
            onCommandSelected = { command ->
                viewModel.setInputText(command)
                showCommandsSheet = false
            }
        )
    }
}

@Composable
private fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onCommandsClick: () -> Unit,
    hasApprovalPending: Boolean
) {
    Surface(
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BadgedBox(
                badge = {
                    if (hasApprovalPending) {
                        Badge()
                    }
                }
            ) {
                FilledIconButton(
                    onClick = onCommandsClick,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        Icons.Rounded.Code,
                        "Commands",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        if (text.startsWith("/")) "Slash command…"
                        else "Ask Spettro anything…",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                shape = MaterialTheme.shapes.extraLarge,
                maxLines = 6,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Default,
                    capitalization = KeyboardCapitalization.Sentences
                )
            )

            FilledIconButton(
                onClick = onSend,
                modifier = Modifier.size(48.dp),
                enabled = text.isNotBlank()
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Send,
                    "Send",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Terminal,
                        null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                "Connected to Spettro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Type a message or tap\nthe commands button to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.GridView, null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Quick tip: /plan to start planning",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
