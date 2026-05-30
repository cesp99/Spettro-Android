package de.aploi.spettrobyeyed.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.aploi.spettrobyeyed.data.models.SpettroEvent
import de.aploi.spettrobyeyed.ui.theme.SuccessGreen
import de.aploi.spettrobyeyed.ui.theme.SuccessGreenContainer
import de.aploi.spettrobyeyed.ui.theme.ToolError
import de.aploi.spettrobyeyed.ui.theme.ToolErrorContainer
import de.aploi.spettrobyeyed.ui.theme.ToolRunning
import de.aploi.spettrobyeyed.ui.theme.ToolRunningContainer
import de.aploi.spettrobyeyed.ui.theme.ToolSuccess
import de.aploi.spettrobyeyed.ui.theme.ToolSuccessContainer
import de.aploi.spettrobyeyed.ui.theme.WarnAmber
import de.aploi.spettrobyeyed.ui.theme.WarnAmberContainer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EventItem(event: SpettroEvent, modifier: Modifier = Modifier) {
    when (event) {
        is SpettroEvent.UserMessage -> UserMessageItem(event, modifier)
        is SpettroEvent.AssistantMessage -> AssistantMessageItem(event, modifier)
        is SpettroEvent.AssistantError -> AssistantErrorItem(event, modifier)
        is SpettroEvent.Tool -> ToolEventItem(event, modifier)
        is SpettroEvent.Comment -> CommentEventItem(event, modifier)
        is SpettroEvent.Plan -> PlanEventItem(event, modifier)
        is SpettroEvent.PlanError -> PlanErrorItem(event, modifier)
        is SpettroEvent.ApprovalRequest -> ApprovalRequestItem(event, modifier)
        is SpettroEvent.AskUser -> AskUserItem(event, modifier)
        is SpettroEvent.Banner -> BannerItem(event, modifier)
        is SpettroEvent.State -> StateChangeItem(event, modifier)
        is SpettroEvent.Unknown -> Unit
    }
}

@Composable
private fun UserMessageItem(event: SpettroEvent.UserMessage, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(
                    topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp
                ),
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Text(
                    text = event.content,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = formatTime(event.at),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
private fun AssistantMessageItem(event: SpettroEvent.AssistantMessage, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(true) }
    var showThinking by remember { mutableStateOf(false) }
    val isLong = event.content.length > 600

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "S",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(
                    topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp
                ),
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .animateContentSize(spring())
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = if (isLong && !expanded)
                            event.content.take(400) + "…"
                        else event.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isLong) {
                        TextButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(if (expanded) "Show less" else "Show more")
                        }
                    }
                    if (event.thinkingContent != null) {
                        TextButton(
                            onClick = { showThinking = !showThinking },
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(if (showThinking) "Hide thinking" else "Show thinking")
                        }
                        AnimatedVisibility(visible = showThinking) {
                            Text(
                                text = event.thinkingContent,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontStyle = FontStyle.Italic
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.padding(start = 40.dp, top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                formatTime(event.at),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            if (event.tokensUsed > 0) {
                Text(
                    "${event.tokensUsed} tokens",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun AssistantErrorItem(event: SpettroEvent.AssistantError, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Error, null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    "Agent Error",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    event.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ToolEventItem(event: SpettroEvent.Tool, modifier: Modifier = Modifier) {
    var outputExpanded by remember { mutableStateOf(false) }

    val (containerColor, contentColor, statusIcon) = when (event.status) {
        "success" -> Triple(ToolSuccessContainer, ToolSuccess, Icons.Rounded.CheckCircle)
        "error" -> Triple(ToolErrorContainer, ToolError, Icons.Rounded.Error)
        else -> Triple(ToolRunningContainer, ToolRunning, Icons.Rounded.Build)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .background(containerColor)
                .clickable(enabled = event.output != null) { outputExpanded = !outputExpanded }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                statusIcon, null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = event.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (event.agent.isNotEmpty()) {
                Text(
                    event.agent,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
            if (event.output != null) {
                Icon(
                    if (outputExpanded) Icons.Rounded.TaskAlt else Icons.Rounded.PendingActions,
                    null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        AnimatedVisibility(visible = outputExpanded && event.output != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = event.output ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 20
                )
            }
        }
    }
}

@Composable
private fun CommentEventItem(event: SpettroEvent.Comment, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.Comment, null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = event.message,
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlanEventItem(event: SpettroEvent.Plan, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(spring()),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.TaskAlt, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Plan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            AnimatedVisibility(visible = expanded) {
                Text(
                    text = event.plan,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            if (expanded) {
                Text(
                    "Use /approve in terminal to execute",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PlanErrorItem(event: SpettroEvent.PlanError, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Error, null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    "Plan Failed",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    event.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ApprovalRequestItem(event: SpettroEvent.ApprovalRequest, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = WarnAmberContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Warning, null,
                    tint = WarnAmber,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "Approval Needed",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = WarnAmber
                )
            }

            Spacer(Modifier.size(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.Black.copy(alpha = 0.08f))
                    .horizontalScroll(rememberScrollState())
                    .padding(10.dp)
            ) {
                Text(
                    text = "$ ${event.command}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = WarnAmber
                )
            }

            if (event.reason.isNotEmpty()) {
                Text(
                    "Reason: ${event.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarnAmber.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Text(
                "Handle this request in the terminal",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = WarnAmber,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun AskUserItem(event: SpettroEvent.AskUser, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Quiz, null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "Input Needed",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Text(
                event.question,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(top = 10.dp)
            )

            if (event.context != null) {
                Text(
                    event.context,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (event.options.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    event.options.forEach { option ->
                        AssistChip(
                            onClick = {},
                            label = { Text(option) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                labelColor = MaterialTheme.colorScheme.onSecondary
                            )
                        )
                    }
                }
            }

            Text(
                "Answer this question in the terminal",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun BannerItem(event: SpettroEvent.Banner, modifier: Modifier = Modifier) {
    val (containerColor, contentColor, icon) = when (event.level) {
        "error" -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Rounded.Error
        )
        "warn", "warning" -> Triple(WarnAmberContainer, WarnAmber, Icons.Rounded.Warning)
        "success" -> Triple(SuccessGreenContainer, SuccessGreen, Icons.Rounded.CheckCircle)
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Rounded.Info
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(18.dp))
            Text(
                event.text,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun StateChangeItem(event: SpettroEvent.State, modifier: Modifier = Modifier) {
    if (event.reason.isEmpty() && event.mode.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = buildString {
                if (event.mode.isNotEmpty()) append(event.mode)
                if (event.reason.isNotEmpty()) {
                    if (isNotEmpty()) append(" · ")
                    append(event.reason)
                }
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

private fun formatTime(at: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault())
        formatter.format(Instant.parse(at))
    } catch (e: Exception) {
        ""
    }
}
