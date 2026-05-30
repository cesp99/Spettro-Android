package de.aploi.spettrobyeyed.data.models

data class SpettroStatus(
    val thinking: Boolean = false,
    val mode: String = "",
    val activeAgent: String = "",
    val messagesCount: Int = 0,
    val tokensUsed: Int = 0
)

data class SendMessageResult(
    val accepted: Boolean = false,
    val queued: Boolean = false,
    val note: String = "",
    val error: String = ""
)

sealed class SpettroEvent {
    abstract val seq: Int
    abstract val at: String

    data class State(
        override val seq: Int,
        override val at: String,
        val thinking: Boolean,
        val mode: String,
        val activeAgent: String,
        val reason: String
    ) : SpettroEvent()

    data class UserMessage(
        override val seq: Int,
        override val at: String,
        val content: String
    ) : SpettroEvent()

    data class AssistantMessage(
        override val seq: Int,
        override val at: String,
        val content: String,
        val thinkingContent: String?,
        val tokensUsed: Int
    ) : SpettroEvent()

    data class AssistantError(
        override val seq: Int,
        override val at: String,
        val error: String
    ) : SpettroEvent()

    data class Tool(
        override val seq: Int,
        override val at: String,
        val name: String,
        val status: String,
        val agent: String,
        val output: String?
    ) : SpettroEvent()

    data class Comment(
        override val seq: Int,
        override val at: String,
        val message: String
    ) : SpettroEvent()

    data class Plan(
        override val seq: Int,
        override val at: String,
        val plan: String
    ) : SpettroEvent()

    data class PlanError(
        override val seq: Int,
        override val at: String,
        val error: String
    ) : SpettroEvent()

    data class ApprovalRequest(
        override val seq: Int,
        override val at: String,
        val command: String,
        val toolId: String,
        val reason: String
    ) : SpettroEvent()

    data class AskUser(
        override val seq: Int,
        override val at: String,
        val question: String,
        val options: List<String>,
        val context: String?,
        val allowFreeResponse: Boolean
    ) : SpettroEvent()

    data class Banner(
        override val seq: Int,
        override val at: String,
        val text: String,
        val level: String
    ) : SpettroEvent()

    data class Unknown(
        override val seq: Int,
        override val at: String,
        val kind: String
    ) : SpettroEvent()
}
