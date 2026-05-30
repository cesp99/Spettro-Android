package de.aploi.spettrobyeyed.data

import de.aploi.spettrobyeyed.data.models.SendMessageResult
import de.aploi.spettrobyeyed.data.models.SpettroEvent
import de.aploi.spettrobyeyed.data.models.SpettroStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SpettroRepository {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val sseClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    suspend fun getStatus(host: String, apiKey: String): Result<SpettroStatus> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("http://$host/status")
                    .header("Authorization", "Bearer $apiKey")
                    .get()
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("HTTP ${response.code}: ${response.message}")
                    )
                }

                val body = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty response"))
                val json = JSONObject(body)

                Result.success(
                    SpettroStatus(
                        thinking = json.optBoolean("thinking", false),
                        mode = json.optString("mode", ""),
                        activeAgent = json.optString("active_agent", ""),
                        messagesCount = json.optInt("messages_count", 0),
                        tokensUsed = json.optInt("tokens_used", 0)
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun sendMessage(host: String, apiKey: String, message: String): Result<SendMessageResult> =
        withContext(Dispatchers.IO) {
            try {
                val bodyJson = JSONObject().put("message", message).toString()
                val body = bodyJson.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("http://$host/messages")
                    .header("Authorization", "Bearer $apiKey")
                    .post(body)
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: "{}"
                val json = JSONObject(responseBody)

                Result.success(
                    SendMessageResult(
                        accepted = json.optBoolean("accepted", false),
                        queued = json.optBoolean("queued", false),
                        note = json.optString("note", ""),
                        error = json.optString("error", "")
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun interrupt(host: String, apiKey: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val body = "".toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("http://$host/interrupt")
                    .header("Authorization", "Bearer $apiKey")
                    .post(body)
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP ${response.code}"))
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun streamEvents(host: String, apiKey: String): Flow<Result<SpettroEvent>> = callbackFlow {
        val request = Request.Builder()
            .url("http://$host/events")
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "text/event-stream")
            .get()
            .build()

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {}

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                if (data.isBlank()) return
                val event = parseEvent(data) ?: return
                trySend(Result.success(event))
            }

            override fun onClosed(eventSource: EventSource) {
                channel.close()
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?
            ) {
                val error = t ?: Exception("SSE failed: ${response?.code}")
                channel.close(error)
            }
        }

        val eventSource = EventSources.createFactory(sseClient).newEventSource(request, listener)
        awaitClose { eventSource.cancel() }
    }

    private fun parseEvent(data: String): SpettroEvent? {
        return try {
            val json = JSONObject(data)
            val seq = json.optInt("seq", 0)
            val kind = json.optString("kind", "unknown")
            val at = json.optString("at", "")
            val eventData = json.optJSONObject("data") ?: JSONObject()

            when (kind) {
                "state" -> SpettroEvent.State(
                    seq = seq, at = at,
                    thinking = eventData.optBoolean("thinking", false),
                    mode = eventData.optString("mode", ""),
                    activeAgent = eventData.optString("active_agent", ""),
                    reason = eventData.optString("reason", "")
                )
                "user_message", "remote_prompt", "remote_command" -> SpettroEvent.UserMessage(
                    seq = seq, at = at,
                    content = eventData.optString("content",
                        eventData.optString("prompt",
                            eventData.optString("command", "")))
                )
                "assistant_message" -> SpettroEvent.AssistantMessage(
                    seq = seq, at = at,
                    content = eventData.optString("content", ""),
                    thinkingContent = eventData.optString("thinking").takeIf { it.isNotEmpty() },
                    tokensUsed = eventData.optInt("tokens_used", 0)
                )
                "assistant_error" -> SpettroEvent.AssistantError(
                    seq = seq, at = at,
                    error = eventData.optString("error", "Unknown error")
                )
                "tool" -> SpettroEvent.Tool(
                    seq = seq, at = at,
                    name = eventData.optString("name", ""),
                    status = eventData.optString("status", "running"),
                    agent = eventData.optString("agent", ""),
                    output = eventData.optString("output").takeIf { it.isNotEmpty() }
                )
                "comment" -> SpettroEvent.Comment(
                    seq = seq, at = at,
                    message = eventData.optString("message", "")
                )
                "plan" -> SpettroEvent.Plan(
                    seq = seq, at = at,
                    plan = eventData.optString("plan", "")
                )
                "plan_error" -> SpettroEvent.PlanError(
                    seq = seq, at = at,
                    error = eventData.optString("error", "")
                )
                "approval_request" -> SpettroEvent.ApprovalRequest(
                    seq = seq, at = at,
                    command = eventData.optString("command", ""),
                    toolId = eventData.optString("tool_id", ""),
                    reason = eventData.optString("reason", "")
                )
                "ask_user" -> {
                    val optionsArray = eventData.optJSONArray("options") ?: JSONArray()
                    val options = (0 until optionsArray.length()).map { optionsArray.getString(it) }
                    SpettroEvent.AskUser(
                        seq = seq, at = at,
                        question = eventData.optString("question", ""),
                        options = options,
                        context = eventData.optString("context").takeIf { it.isNotEmpty() },
                        allowFreeResponse = eventData.optBoolean("allow_free_response", true)
                    )
                }
                "banner" -> SpettroEvent.Banner(
                    seq = seq, at = at,
                    text = eventData.optString("text", ""),
                    level = eventData.optString("level", "info")
                )
                "remote_started", "remote_stopped", "system_message",
                "commit", "commit_error", "search", "search_error",
                "remote_interrupt" -> SpettroEvent.Unknown(seq = seq, at = at, kind = kind)
                else -> SpettroEvent.Unknown(seq = seq, at = at, kind = kind)
            }
        } catch (e: JSONException) {
            null
        }
    }
}
