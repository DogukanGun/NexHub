package com.dag.solai.tools.misc.allora

import com.dag.solai.BuildConfig
import dev.langchain4j.agent.tool.Tool
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.call.*
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.runBlocking

class AlloraTools {
    private val client = HttpClient(CIO)

    @Tool("Returns the latest model predictions from Allora Network")
    fun getModelPredictions(modelId: String): String {
        val url = "https://api.allora.network/v2/predictions/${modelId}?x_allora_key=${BuildConfig.ALLORA_API_KEY}"
        var body: String
        runBlocking {
            val httpResponse: HttpResponse = client.get(url)
            body = httpResponse.body()
        }
        return body
    }

    @Tool("Returns the list of available models on Allora Network")
    fun getAvailableModels(): String {
        val url = "https://api.allora.network/v2/models?x_allora_key=${BuildConfig.ALLORA_API_KEY}"
        var body: String
        runBlocking {
            val httpResponse: HttpResponse = client.get(url)
            body = httpResponse.body()
        }
        return body
    }

    @Tool("Returns the performance metrics for a specific model")
    fun getModelMetrics(modelId: String): String {
        val url = "https://api.allora.network/v2/models/${modelId}/metrics?x_allora_key=${BuildConfig.ALLORA_API_KEY}"
        var body: String
        runBlocking {
            val httpResponse: HttpResponse = client.get(url)
            body = httpResponse.body()
        }
        return body
    }

    @Tool("Returns the historical predictions for a specific model")
    fun getHistoricalPredictions(modelId: String, startDate: String, endDate: String): String {
        val url = "https://api.allora.network/v2/models/${modelId}/history" +
                "?start_date=${startDate}" +
                "&end_date=${endDate}" +
                "&x_allora_key=${BuildConfig.ALLORA_API_KEY}"
        var body: String
        runBlocking {
            val httpResponse: HttpResponse = client.get(url)
            body = httpResponse.body()
        }
        return body
    }

    @Tool("Returns the current status of the Allora Network")
    fun getNetworkStatus(): String {
        val url = "https://api.allora.network/v2/status?x_allora_key=${BuildConfig.ALLORA_API_KEY}"
        var body: String
        runBlocking {
            val httpResponse: HttpResponse = client.get(url)
            body = httpResponse.body()
        }
        return body
    }

    @Tool("Returns the latest model maker information")
    fun getModelMakerInfo(): String {
        val url = "https://api.allora.network/v2/model-maker/info?x_allora_key=${BuildConfig.ALLORA_API_KEY}"
        var body: String
        runBlocking {
            val httpResponse: HttpResponse = client.get(url)
            body = httpResponse.body()
        }
        return body
    }
}
