package com.example.agent.tools.defi.dexscreener

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class DexScreenerTools {
    private val client = HttpClient(CIO)

    @Serializable
    data class JupiterTokenData(
        val address: String,
        val chainId: Int,
        val decimals: Int,
        val name: String,
        val symbol: String,
        @SerialName("logoURI")
        val logoUri: String? = null,
        val tags: List<String>? = null,
        val extensions: Map<String, String>? = null
    )

    @Serializable
    data class DexScreenerBaseToken(
        val address: String,
        val name: String,
        val symbol: String
    )

    @Serializable
    data class DexScreenerPair(
        val chainId: String,
        @SerialName("baseToken")
        val baseToken: DexScreenerBaseToken,
        val fdv: Double? = null
    )

    @Serializable
    data class DexScreenerResponse(
        val pairs: List<DexScreenerPair>? = null
    )

    suspend fun getTokenDataByAddress(mintAddress: String): String {
        return try {
            if (mintAddress.isBlank()) {
                throw Exception("Mint address is required")
            }

            val response = client.get("https://tokens.jup.ag/token/${mintAddress}") {
                contentType(ContentType.Application.Json)
            }
            
            val token = response.body<JupiterTokenData>()
            """
            Token Data:
            Name: ${token.name}
            Symbol: ${token.symbol}
            Address: ${token.address}
            Decimals: ${token.decimals}
            Logo URI: ${token.logoUri ?: "N/A"}
            Tags: ${token.tags?.joinToString(", ") ?: "N/A"}
            """.trimIndent()
        } catch (e: Exception) {
            "Error fetching token data: ${e.message}"
        }
    }

    suspend fun getTokenAddressFromTicker(ticker: String): String {
        return try {
            val response = client.get("https://api.dexscreener.com/latest/dex/search?q=${ticker}") {
                contentType(ContentType.Application.Json)
            }
            
            val data = response.body<DexScreenerResponse>()
            
            if (data.pairs.isNullOrEmpty()) {
                return "No pairs found for ticker: $ticker"
            }

            // Filter for Solana pairs and sort by FDV
            val solanaPairs = data.pairs
                .filter { it.chainId == "solana" }
                .filter { it.baseToken.symbol.equals(ticker, ignoreCase = true) }
                .sortedByDescending { it.fdv ?: 0.0 }

            if (solanaPairs.isEmpty()) {
                return "No Solana pairs found for ticker: $ticker"
            }

            "Token address for $ticker: ${solanaPairs[0].baseToken.address}"
        } catch (e: Exception) {
            "Error fetching token address: ${e.message}"
        }
    }

    suspend fun getTokenDataByTicker(ticker: String): String {
        return try {
            val addressResponse = client.get("https://api.dexscreener.com/latest/dex/search?q=${ticker}") {
                contentType(ContentType.Application.Json)
            }
            
            val data = addressResponse.body<DexScreenerResponse>()
            
            if (data.pairs.isNullOrEmpty()) {
                return "No pairs found for ticker: $ticker"
            }

            val solanaPairs = data.pairs
                .filter { it.chainId == "solana" }
                .filter { it.baseToken.symbol.equals(ticker, ignoreCase = true) }
                .sortedByDescending { it.fdv ?: 0.0 }

            if (solanaPairs.isEmpty()) {
                return "No Solana pairs found for ticker: $ticker"
            }

            val address = solanaPairs[0].baseToken.address
            getTokenDataByAddress(address)
        } catch (e: Exception) {
            "Error fetching token data: ${e.message}"
        }
    }
}
