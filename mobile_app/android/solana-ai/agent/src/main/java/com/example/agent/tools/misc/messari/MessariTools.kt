package com.example.agent.tools.misc.messari

import com.dag.wallet.BuildConfig
import dev.langchain4j.agent.tool.Tool
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*

class MessariTools {
    private val client = HttpClient(CIO)
    private val baseUrl = "https://api.messari.io/ai/v1"

    private fun checkApiKey() {
        if (BuildConfig.MESSARI_API_KEY.isNullOrEmpty()) {
            throw IllegalStateException("MESSARI_API_KEY is not configured in BuildConfig.")
        }
    }

    @Tool("Ask a question to Messari's AI and get a response")
    fun askMessariAi(question: String): String {
        checkApiKey()
        var body: String
        runBlocking {
            try {
                val response: HttpResponse = client.post("$baseUrl/chat/completions") {
                    contentType(ContentType.Application.Json)
                    header("x-messari-api-key", BuildConfig.MESSARI_API_KEY)
                    setBody(buildJsonObject {
                        putJsonArray("messages") {
                            addJsonObject {
                                put("role", "user")
                                put("content", question)
                            }
                        }
                    }.toString())
                }

                // Parse the response to extract just the content from the first message
                val jsonResponse = Json.parseToJsonElement(response.body<String>())
                val content = jsonResponse.jsonObject["data"]
                    ?.jsonObject?.get("messages")
                    ?.jsonArray?.getOrNull(0)
                    ?.jsonObject?.get("content")
                    ?.jsonPrimitive?.content

                body = content ?: throw IllegalStateException("No content found in response")
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is IllegalStateException -> e.message
                    else -> "Error fetching data from Messari: ${e.message}"
                }
                throw IllegalStateException(errorMessage)
            }
        }
        return body
    }
}
