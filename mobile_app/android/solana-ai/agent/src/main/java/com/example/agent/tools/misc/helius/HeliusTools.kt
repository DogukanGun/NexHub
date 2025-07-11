package com.example.agent.tools.misc.helius

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

class HeliusTools {
    private val client = HttpClient(CIO)
    private val baseUrl = "https://api.helius.xyz/v0"
    private val rpcUrl = "https://mainnet.helius-rpc.com"

    private fun checkApiKey() {
        if (BuildConfig.HELIUS_API_KEY.isNullOrEmpty()) {
            throw IllegalStateException("HELIUS_API_KEY is not configured in BuildConfig.")
        }
    }

    @Tool("Create a new Helius webhook for monitoring transactions")
    fun createWebhook(accountAddresses: List<String>, webhookURL: String): String {
        checkApiKey()
        var body: String
        runBlocking {
            val response: HttpResponse = client.post("$baseUrl/webhooks") {
                parameter("api-key", BuildConfig.HELIUS_API_KEY)
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("webhookURL", webhookURL)
                    putJsonArray("transactionTypes") { add("Any") }
                    putJsonArray("accountAddresses") { 
                        accountAddresses.forEach { add(it) }
                    }
                    put("webhookType", "enhanced")
                    put("txnStatus", "all")
                }.toString())
            }
            body = response.body()
        }
        return body
    }

    @Tool("Get details of an existing Helius webhook by ID")
    fun getWebhook(webhookId: String): String {
        checkApiKey()
        var body: String
        runBlocking {
            val response: HttpResponse = client.get("$baseUrl/webhooks/$webhookId") {
                parameter("api-key", BuildConfig.HELIUS_API_KEY)
                contentType(ContentType.Application.Json)
            }
            body = response.body()
        }
        return body
    }

    @Tool("Delete an existing Helius webhook by ID")
    fun deleteWebhook(webhookId: String): String {
        checkApiKey()
        var body: String
        runBlocking {
            val response: HttpResponse = client.delete("$baseUrl/webhooks/$webhookId") {
                parameter("api-key", BuildConfig.HELIUS_API_KEY)
                contentType(ContentType.Application.Json)
            }
            body = if (response.status == HttpStatusCode.NoContent) {
                """{"message": "Webhook deleted successfully (no content returned)"}"""
            } else {
                response.body()
            }
        }
        return body
    }

    @Tool("Parse a Solana transaction using Helius Enhanced Transactions API")
    fun parseTransaction(transactionId: String): String {
        checkApiKey()
        var body: String
        runBlocking {
            val response: HttpResponse = client.post("$baseUrl/transactions/") {
                parameter("api-key", BuildConfig.HELIUS_API_KEY)
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    putJsonArray("transactions") { add(transactionId) }
                }.toString())
            }
            body = response.body()
        }
        return body
    }

    @Tool("Get assets owned by a specific Solana wallet address")
    fun getAssetsByOwner(
        ownerAddress: String,
        limit: Int,
        page: Int = 1,
        showFungible: Boolean = true,
        sortBy: String? = null,
        sortDirection: String? = null
    ): String {
        checkApiKey()
        var body: String
        runBlocking {
            val response: HttpResponse = client.post(rpcUrl) {
                parameter("api-key", BuildConfig.HELIUS_API_KEY)
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("jsonrpc", "2.0")
                    put("id", "get-assets")
                    put("method", "getAssetsByOwner")
                    putJsonObject("params") {
                        put("ownerAddress", ownerAddress)
                        put("page", page)
                        put("limit", limit)
                        putJsonObject("displayOptions") {
                            put("showFungible", showFungible)
                        }
                        if (sortBy != null && sortDirection != null) {
                            putJsonObject("sortOptions") {
                                put("sortBy", sortBy)
                                put("sortDirection", sortDirection)
                            }
                        }
                    }
                }.toString())
            }
            body = response.body()
        }
        return body
    }
}
