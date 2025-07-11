package com.example.agent.tools.defi.rugcheck

import dev.langchain4j.agent.tool.Tool
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class Rugcheck {
    private val client = HttpClient(CIO)
    private val baseUrl = "https://api.rugcheck.xyz/v1"

    @Serializable
    data class TokenMetadata(
        val name: String? = null,
        val symbol: String? = null,
        val mint: String,
        val decimals: Int? = null,
        @SerialName("total_supply")
        val totalSupply: String? = null,
        @SerialName("holder_count")
        val holderCount: Int? = null
    )

    @Serializable
    data class RiskScore(
        val value: Double,
        val category: String,
        val description: String? = null
    )

    @Serializable
    data class TokenCheck(
        val metadata: TokenMetadata,
        @SerialName("risk_score")
        val riskScore: RiskScore? = null,
        @SerialName("is_verified")
        val isVerified: Boolean? = null,
        @SerialName("is_mintable")
        val isMintable: Boolean? = null,
        @SerialName("has_freeze_authority")
        val hasFreezeAuthority: Boolean? = null,
        @SerialName("has_close_authority")
        val hasCloseAuthority: Boolean? = null,
        val warnings: List<String>? = null,
        @SerialName("created_at")
        val createdAt: String? = null,
        @SerialName("last_updated")
        val lastUpdated: String? = null
    )

    @Tool("Get a summary report for a token from Rugcheck")
    suspend fun getTokenReportSummary(mint: String): String {
        return try {
            val response = client.get("$baseUrl/tokens/$mint/report/summary")
            val report = response.body<TokenCheck>()
            formatTokenReport(report)
        } catch (e: Exception) {
            "Error fetching report summary for token $mint: ${e.message}"
        }
    }

    @Tool("Get a detailed report for a token from Rugcheck")
    suspend fun getTokenDetailedReport(mint: String): String {
        return try {
            val response = client.get("$baseUrl/tokens/$mint/report")
            val report = response.body<TokenCheck>()
            formatTokenReport(report)
        } catch (e: Exception) {
            "Error fetching detailed report for token $mint: ${e.message}"
        }
    }

    private fun formatTokenReport(report: TokenCheck): String {
        return buildString {
            appendLine("Token Report:")
            appendLine("Name: ${report.metadata.name ?: "N/A"}")
            appendLine("Symbol: ${report.metadata.symbol ?: "N/A"}")
            appendLine("Mint: ${report.metadata.mint}")
            appendLine("Decimals: ${report.metadata.decimals ?: "N/A"}")
            appendLine("Total Supply: ${report.metadata.totalSupply ?: "N/A"}")
            appendLine("Holder Count: ${report.metadata.holderCount ?: "N/A"}")
            appendLine()
            
            report.riskScore?.let {
                appendLine("Risk Assessment:")
                appendLine("Score: ${it.value}")
                appendLine("Category: ${it.category}")
                it.description?.let { desc -> appendLine("Description: $desc") }
                appendLine()
            }

            appendLine("Security Checks:")
            appendLine("Verified: ${report.isVerified ?: "Unknown"}")
            appendLine("Mintable: ${report.isMintable ?: "Unknown"}")
            appendLine("Has Freeze Authority: ${report.hasFreezeAuthority ?: "Unknown"}")
            appendLine("Has Close Authority: ${report.hasCloseAuthority ?: "Unknown"}")
            
            report.warnings?.let {
                if (it.isNotEmpty()) {
                    appendLine()
                    appendLine("Warnings:")
                    it.forEach { warning -> appendLine("- $warning") }
                }
            }
            
            appendLine()
            appendLine("Timestamps:")
            appendLine("Created: ${report.createdAt ?: "N/A"}")
            appendLine("Last Updated: ${report.lastUpdated ?: "N/A"}")
        }
    }
}
