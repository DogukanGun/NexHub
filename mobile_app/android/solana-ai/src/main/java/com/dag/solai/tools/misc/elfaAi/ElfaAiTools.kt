package com.dag.solai.tools.misc.elfaAi

import com.dag.solai.BuildConfig
import dev.langchain4j.agent.tool.Tool
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.call.*
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.runBlocking

class ElfaAiTools {
    private val client = HttpClient(CIO)
    private val baseUrl = "https://api.elfa.ai"

    private fun checkApiKey() {
        if (BuildConfig.ELFA_AI_API_KEY.isNullOrEmpty()) {
            throw IllegalStateException("ELFA_AI_API_KEY is not configured in BuildConfig.")
        }
    }

    private suspend fun makeRequest(endpoint: String, params: Map<String, Any?> = emptyMap()): String {
        checkApiKey()
        val queryParams = params.entries
            .filter { it.value != null }
            .joinToString("&") { "${it.key}=${it.value}" }
        val url = "$baseUrl$endpoint${if (queryParams.isNotEmpty()) "?$queryParams" else ""}"
        
        val response: HttpResponse = client.get(url) {
            header("x-elfa-api-key", BuildConfig.ELFA_AI_API_KEY)
            header("Content-Type", "application/json")
        }
        return response.body()
    }

    @Tool("Ping the Elfa AI API to check if it's available")
    fun pingElfaAiApi(): String {
        var body: String
        runBlocking {
            body = makeRequest("/v1/ping")
        }
        return body
    }

    @Tool("Get the status of your Elfa AI API key")
    fun getElfaAiApiKeyStatus(): String {
        var body: String
        runBlocking {
            body = makeRequest("/v1/key-status")
        }
        return body
    }

    @Tool("Get smart mentions with pagination")
    fun getSmartMentions(limit: Int = 100, offset: Int = 0): String {
        var body: String
        runBlocking {
            body = makeRequest("/v1/mentions", mapOf(
                "limit" to limit,
                "offset" to offset
            ))
        }
        return body
    }

    @Tool("Get top mentions for a specific ticker")
    fun getTopMentionsByTicker(
        ticker: String,
        timeWindow: String = "1h",
        page: Int = 1,
        pageSize: Int = 10,
        includeAccountDetails: Boolean = false
    ): String {
        var body: String
        runBlocking {
            body = makeRequest("/v1/top-mentions", mapOf(
                "ticker" to ticker,
                "timeWindow" to timeWindow,
                "page" to page,
                "pageSize" to pageSize,
                "includeAccountDetails" to includeAccountDetails
            ))
        }
        return body
    }

    @Tool("Search mentions by keywords within a time range")
    fun searchMentionsByKeywords(
        keywords: String,
        from: Long,
        to: Long,
        limit: Int = 20,
        cursor: String? = null
    ): String {
        var body: String
        runBlocking {
            body = makeRequest("/v1/mentions/search", mapOf(
                "keywords" to keywords,
                "from" to from,
                "to" to to,
                "limit" to limit,
                "cursor" to cursor
            ))
        }
        return body
    }

    @Tool("Get trending tokens with customizable parameters")
    fun getTrendingTokens(
        timeWindow: String = "24h",
        page: Int = 1,
        pageSize: Int = 50,
        minMentions: Int = 5
    ): String {
        var body: String
        runBlocking {
            body = makeRequest("/v1/trending-tokens", mapOf(
                "timeWindow" to timeWindow,
                "page" to page,
                "pageSize" to pageSize,
                "minMentions" to minMentions
            ))
        }
        return body
    }

    @Tool("Get smart Twitter account statistics for a specific username")
    fun getSmartTwitterAccountStats(username: String): String {
        var body: String
        runBlocking {
            body = makeRequest("/v1/account/smart-stats", mapOf(
                "username" to username
            ))
        }
        return body
    }
}
