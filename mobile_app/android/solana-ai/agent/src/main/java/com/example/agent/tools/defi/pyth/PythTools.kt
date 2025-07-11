package com.example.agent.tools.defi.pyth

import dev.langchain4j.agent.tool.Tool
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigInteger

class PythTools {
    private val client = HttpClient(CIO)
    private val stableHermesServiceUrl = "https://hermes.pyth.network"

    @Serializable
    data class PythPriceFeedAttributes(
        val base: String,
        val quote: String,
        val asset_type: String
    )

    @Serializable
    data class PythPriceFeedIDItem(
        val id: String,
        val attributes: PythPriceFeedAttributes
    )

    @Serializable
    data class PriceInfo(
        val price: String,
        @SerialName("expo")
        val exponent: Int,
        val conf: String? = null,
        val status: String? = null
    )

    @Serializable
    data class ParsedPriceData(
        val id: String,
        val price: PriceInfo,
        val ema_price: PriceInfo? = null,
        val timestamp: Long? = null
    )

    @Serializable
    data class PythPriceResponse(
        val parsed: List<ParsedPriceData>
    )

    @Tool("Fetch price feed ID for a given token symbol from Pyth")
    suspend fun fetchPythPriceFeedID(tokenSymbol: String): String {
        return try {
            val response = client.get("$stableHermesServiceUrl/v2/price_feeds") {
                parameter("query", tokenSymbol)
                parameter("asset_type", "crypto")
            }

            val data = response.body<List<PythPriceFeedIDItem>>()

            if (data.isEmpty()) {
                return "No price feed found for $tokenSymbol"
            }

            if (data.size > 1) {
                val filteredData = data.filter { 
                    it.attributes.base.equals(tokenSymbol, ignoreCase = true) 
                }

                if (filteredData.isEmpty()) {
                    return "No price feed found for $tokenSymbol"
                }

                return "Price Feed ID for $tokenSymbol: ${filteredData[0].id}"
            }

            "Price Feed ID for $tokenSymbol: ${data[0].id}"
        } catch (e: Exception) {
            "Error fetching price feed ID from Pyth: ${e.message}"
        }
    }

    @Tool("Fetch the current price for a given Pyth price feed ID")
    suspend fun fetchPythPrice(feedID: String): String {
        return try {
            val response = client.get("$stableHermesServiceUrl/v2/updates/price/latest") {
                parameter("ids[]", feedID)
            }

            val data = response.body<PythPriceResponse>()

            if (data.parsed.isEmpty()) {
                return "No price data found for $feedID"
            }

            val priceData = data.parsed[0]
            val price = BigInteger(priceData.price.price)
            val exponent = priceData.price.exponent

            val formattedPrice = if (exponent < 0) {
                val adjustedPrice = price.multiply(BigInteger.valueOf(100))
                val divisor = BigInteger.TEN.pow(-exponent)
                val scaledPrice = adjustedPrice.divide(divisor)
                
                val priceStr = scaledPrice.toString()
                var formattedPrice = "${priceStr.substring(0, priceStr.length - 2)}.${priceStr.substring(priceStr.length - 2)}"
                if (formattedPrice.startsWith(".")) {
                    formattedPrice = "0$formattedPrice"
                }
                formattedPrice
            } else {
                val scaledPrice = price.divide(BigInteger.TEN.pow(exponent))
                scaledPrice.toString()
            }

            """
            Price Data for feed $feedID:
            Price: $formattedPrice
            Confidence: ${priceData.price.conf ?: "N/A"}
            Status: ${priceData.price.status ?: "N/A"}
            Timestamp: ${priceData.timestamp ?: "N/A"}
            """.trimIndent()
        } catch (e: Exception) {
            "Error fetching price from Pyth: ${e.message}"
        }
    }

    @Tool("Get both price feed ID and current price for a token symbol")
    suspend fun getTokenPriceBySymbol(tokenSymbol: String): String {
        return try {
            val feedIdResponse = client.get("$stableHermesServiceUrl/v2/price_feeds") {
                parameter("query", tokenSymbol)
                parameter("asset_type", "crypto")
            }

            val feedData = feedIdResponse.body<List<PythPriceFeedIDItem>>()

            if (feedData.isEmpty()) {
                return "No price feed found for $tokenSymbol"
            }

            val feedId = feedData.firstOrNull { 
                it.attributes.base.equals(tokenSymbol, ignoreCase = true) 
            }?.id ?: feedData[0].id

            fetchPythPrice(feedId)
        } catch (e: Exception) {
            "Error fetching token price: ${e.message}"
        }
    }
}
