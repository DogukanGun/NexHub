package com.example.agent.tools.defi.jupiter

import androidx.fragment.app.FragmentActivity
import com.dag.wallet.SolanaWalletManager
import com.solana.core.PublicKey
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

class JupiterTools(
    private val walletManager: SolanaWalletManager,
    private val activity: FragmentActivity
) {
    private val client = HttpClient(CIO)
    private val jupiterApi = "https://quote-api.jup.ag/v6"
    
    companion object {
        val SOL = PublicKey("So11111111111111111111111111111111111111112")
        val USDC = PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v")
        const val DEFAULT_SLIPPAGE_BPS = 300
        const val JUP_REFERRAL_ADDRESS = "REFER4ZgmyYx9c6K9g6pZqJjjGbkEEynrBmGZKUVm"
    }

    @Serializable
    data class QuoteResponse(
        val inputMint: String,
        val outputMint: String,
        val amount: String,
        val otherAmountThreshold: String,
        val swapMode: String,
        val slippageBps: Int,
        val platformFee: PlatformFee? = null,
        val priceImpactPct: Double,
        val routePlan: List<RoutePlanStep>,
        val contextSlot: Long,
        val timeTaken: Double
    )

    @Serializable
    data class PlatformFee(
        val amount: String,
        val feeBps: Int
    )

    @Serializable
    data class RoutePlanStep(
        val swapInfo: SwapInfo,
        val percent: Int
    )

    @Serializable
    data class SwapInfo(
        val ammKey: String,
        val label: String,
        val inputMint: String,
        val outputMint: String,
        val inAmount: String,
        val outAmount: String,
        val feeAmount: String,
        val feeMint: String
    )

    @Serializable
    data class SwapRequest(
        val quoteResponse: QuoteResponse,
        val userPublicKey: String,
        val wrapAndUnwrapSol: Boolean = true,
        val dynamicComputeUnitLimit: Boolean = true,
        val dynamicSlippage: Boolean = true,
        val prioritizationFeeLamports: PrioritizationFee,
        val feeAccount: String? = null
    )

    @Serializable
    data class PrioritizationFee(
        val priorityLevelWithMaxLamports: PriorityLevel
    )

    @Serializable
    data class PriorityLevel(
        val maxLamports: Long = 10000000,
        val global: Boolean = false,
        val priorityLevel: String = "medium"
    )

    @Serializable
    data class SwapResponse(
        @SerialName("swapTransaction")
        val swapTransaction: String
    )

    suspend fun stakeWithJup(amount: Long): String {
        return try {
            val response = client.post("https://worker.jup.ag/blinks/swap/So11111111111111111111111111111111111111112/jupSoLaHXQiZZTSfEWMTRRgpnyFm8f6sZdosWBjx93v/$amount") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "account" to walletManager.getPublicKey().publicKey.toBase58()
                ))
            }

            val transactionData = response.body<Map<String, String>>()
            val transactionBase64 = transactionData["transaction"] ?: throw Exception("No transaction data received")

            walletManager.signTransaction(
                txAsBase58 = transactionBase64,
                activity = activity,
                onResult = { signedTx ->
                    "Successfully staked $amount SOL with Jupiter. Transaction: ${signedTx.toString()}"
                },
                onFailure = {
                    throw Exception("Failed to sign transaction")
                }
            )
            "Staking initiated"
        } catch (e: Exception) {
            "Failed to stake SOL: ${e.message}"
        }
    }

    suspend fun trade(
        outputMint: String,
        inputAmount: Double,
        inputMint: String = USDC.toBase58()
    ): String {
        return try {
            // Determine if input is native SOL
            val isNativeSol = inputMint == SOL.toBase58()
            val inputDecimals = if (isNativeSol) 9 else getMintDecimals(inputMint)
            val scaledAmount = (inputAmount * Math.pow(10.0, inputDecimals.toDouble())).toLong()

            // Get quote
            val quoteUrl = buildString {
                append("$jupiterApi/quote?")
                append("inputMint=$inputMint")
                append("&outputMint=$outputMint")
                append("&amount=$scaledAmount")
                append("&dynamicSlippage=true")
                append("&minimizeSlippage=false")
                append("&onlyDirectRoutes=false")
                append("&maxAccounts=64")
                append("&swapMode=ExactIn")
            }

            val quoteResponse = client.get(quoteUrl).body<QuoteResponse>()

            // Get swap transaction
            val swapResponse = client.post("$jupiterApi/swap") {
                contentType(ContentType.Application.Json)
                setBody(SwapRequest(
                    quoteResponse = quoteResponse,
                    userPublicKey = walletManager.getPublicKey().publicKey.toBase58(),
                    prioritizationFeeLamports = PrioritizationFee(
                        PriorityLevel()
                    )
                ))
            }.body<SwapResponse>()

            // Sign transaction
            walletManager.signTransaction(
                txAsBase58 = swapResponse.swapTransaction,
                activity = activity,
                onResult = { signedTx ->
                    "Swap successful. Transaction: ${signedTx.toString()}"
                },
                onFailure = {
                    throw Exception("Failed to sign transaction")
                }
            )
            "Swap initiated"
        } catch (e: Exception) {
            "Failed to execute swap: ${e.message}"
        }
    }

    private suspend fun getMintDecimals(mintAddress: String): Int {
        // You would implement this to fetch mint info from the blockchain
        // For now returning a default value
        return 6 // Default for USDC
    }
}
