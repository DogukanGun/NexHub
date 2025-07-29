package com.example.agent

import androidx.fragment.app.FragmentActivity
import com.dag.wallet.SolanaWalletManager
import com.example.agent.tools.createAlloraTools
import com.example.agent.tools.createCoinGeckoTools
import com.example.agent.tools.createDexScreenerTools
import com.example.agent.tools.createElfaAiTools
import com.example.agent.tools.createGibworkTools
import com.example.agent.tools.createHeliusTools
import com.example.agent.tools.createJupiterTools
import com.example.agent.tools.createMessariTools
import com.example.agent.tools.createMetaplexTools
import com.example.agent.tools.createPythTools
import com.example.agent.tools.createRugCheckTools
import com.example.agent.tools.createWalletTools
import com.example.agent.tools.defi.dexscreener.DexScreenerTools
import com.example.agent.tools.defi.jupiter.JupiterTools
import com.example.agent.tools.defi.pyth.PythTools
import com.example.agent.tools.defi.rugcheck.Rugcheck
import com.example.agent.tools.misc.allora.AlloraTools
import com.example.agent.tools.misc.coingecko.CoinGeckoTools
import com.example.agent.tools.misc.elfaAi.ElfaAiTools
import com.example.agent.tools.misc.gibwork.GibworkTools
import com.example.agent.tools.misc.helius.HeliusTools
import com.example.agent.tools.misc.messari.MessariTools
import com.example.agent.tools.nft.metaplex.MetaplexTools
import com.example.agent.tools.solana.WalletTools
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.content
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.util.UUID

class SolanaAiAgent(
    private val walletManager: SolanaWalletManager,
    private val activity: FragmentActivity
) {

    // Initialize all tools
    private val walletTools = WalletTools(walletManager)
    private val jupiterTools = JupiterTools(walletManager, activity)
    private val dexScreenerTools = DexScreenerTools()
    private val pythTools = PythTools()
    private val rugcheckTools = Rugcheck()
    private val metaplexTools = MetaplexTools(walletManager)
    private val coinGeckoTools = CoinGeckoTools()
    private val alloraTools = AlloraTools()
    private val elfaAiTools = ElfaAiTools()
    private val gibworkTools = GibworkTools(walletManager)
    private val heliusTools = HeliusTools()
    private val messariTools = MessariTools()

    private val memoryMap = mutableMapOf<String, MutableList<Content>>()

    @OptIn(PublicPreviewAPI::class)
    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(
            modelName = "gemini-1.5-flash",
            tools = listOf(
                createDexScreenerTools(),
                createPythTools(),
                createRugCheckTools(),
                createJupiterTools(),
                createMetaplexTools(),
                createHeliusTools(),
                createCoinGeckoTools(),
                createAlloraTools(),
                createMessariTools(),
                createElfaAiTools(),
                createWalletTools(),
                createGibworkTools()
            ).flatten()
        )

    private val systemPrompt = """
            You are a Solana AI Agent specialized in blockchain operations, DeFi, and NFTs.
            
            Your capabilities include:
            
            WALLET OPERATIONS:
            - Create and manage Solana wallets
            - Send SOL and SPL tokens
            - Check balances and token accounts
            - Sign transactions with biometric authentication
            
            DEFI OPERATIONS:
            - Token swapping via Jupiter Exchange
            - Price feeds from Pyth Network
            - Token information from DexScreener
            - Security analysis via Rugcheck
            - SOL staking with Jupiter validator
            
            NFT OPERATIONS:
            - Create and mint NFTs using Metaplex
            - Candy Machine operations (V1 and V2)
            - NFT transfers and collection management
            - Find NFTs by owner, creator, or mint address
            
            MARKET DATA:
            - Real-time price data from CoinGecko
            - Token trends and market analysis
            - AI-powered insights from Messari
            - Social sentiment from Elfa AI
            - Predictive analytics from Allora Network
            
            ADDITIONAL SERVICES:
            - Create tasks on Gibwork platform
            - Monitor transactions with Helius webhooks
            - Enhanced transaction parsing
            - Asset management and portfolio tracking
            
            Always prioritize security and ask for confirmation before executing transactions.
            Provide clear, helpful responses and explain complex operations step by step.
            When handling financial operations, always double-check addresses and amounts.
            
            Current wallet context:
            - Connected wallet: ${walletManager.getPublicKey().publicKey.toBase58()}
            - Network: Solana Mainnet
            - Available tools: Wallet management, DeFi operations, NFT operations, Market data
        """.trimIndent()

    private fun getMemory(id: String): MutableList<Content> =
        memoryMap.getOrPut(id) { mutableListOf() }

    private fun createMemory(): String {
        val newId = UUID.randomUUID().toString()
        memoryMap[newId] = mutableListOf()
        return newId
    }

    /**
     * Main interaction method. It's now a suspend function.
     */
    suspend fun ask(message: String, messageHistoryId: String? = null): ChatResponse {
        val historyId = messageHistoryId ?: createMemory()
        val history = getMemory(historyId)

        // Add the new user message to the history
        history.add(content(role = "user") { text(message) })

        while (true) {
            // Send history to the model
            val response = model.generateContent(*history.toTypedArray())

            val responsePart = response.candidates.first().content.parts.first()

            when (responsePart) {
                // Final answer from the model
                is TextPart -> {
                    history.add(response.candidates.first().content)
                    // Trim history if it's too long
                    if (history.size > MAX_HISTORY) {
                        memoryMap[historyId] = history.takeLast(MAX_HISTORY).toMutableList()
                    }
                    return ChatResponse(response = responsePart.text, id = historyId)
                }
                // Model wants to call a function
                is FunctionCallPart -> {
                    // Add the function call to history
                    history.add(response.candidates.first().content)
                    // Execute the function
                    val toolResponse = executeToolCall(responsePart)
                    // Add the function's response to history
                    history.add(content(role = "tool") { part(toolResponse) })
                    // Loop again to get the model's final answer based on the tool's output
                }
            }
        }
    }

    /**
     * Executes a tool call requested by the model.
     */
    private suspend fun executeToolCall(functionCall: FunctionCallPart): FunctionResponsePart {
        val functionName = functionCall.name
        val args = functionCall.args
        val result = try {
            withTimeout(REQUEST_TIMEOUT) {

            }
        } catch (e: TimeoutCancellationException) {
            "Error: The request to the tool timed out."
        } catch (e: Exception) {
            "Error executing tool '$functionName': ${e.message}"
        }
        return FunctionResponsePart(functionName, result as JsonObject)
    }
    /**
     * Send a message to the Solana AI Agent and get a response
     * @param message The user's message
     * @param onResponse Callback for when response is received
     * @param onError Callback for when an error occurs
     */
    suspend fun sendMessage(
        message: String,
        messageHistoryId: String?,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Process the message through AI service
            val response = ask(message,messageHistoryId)
            onResponse(response.response)
        } catch (e: Exception) {
            onError("Error processing message: ${e.message}")
        }
    }

    /**
     * Get wallet information
     */
    fun getWalletInfo(): String {
        return try {
            """
            Wallet Information:
            - Public Key: ${walletManager.getPublicKey().publicKey.toBase58()}
            - Network: Solana Mainnet
            - Available Tools: ${getAllAvailableTools().size} tools loaded
            """.trimIndent()
        } catch (e: Exception) {
            "Error getting wallet info: ${e.message}"
        }
    }


    /**
     * Execute a quick wallet operation
     */
    fun quickWalletOperation(operation: String): String {
        return when (operation.lowercase()) {
            "balance" -> walletTools.getTokenWallets()
            "keys" -> walletTools.getPublicKeys(activity)
            "tokens" -> runBlocking { walletTools.getSupportedTokens().toString() }
            else -> "Unknown operation. Available: balance, keys, tokens"
        }
    }

    /**
     * Get all available tools
     */
    private fun getAllAvailableTools(): List<Any> {
        return listOf(
            walletTools,
            jupiterTools,
            dexScreenerTools,
            pythTools,
            rugcheckTools,
            metaplexTools,
            coinGeckoTools,
            alloraTools,
            elfaAiTools,
            gibworkTools,
            heliusTools,
            messariTools
        )
    }

    companion object {
        internal val json = Json { ignoreUnknownKeys = true }
        private const val REQUEST_TIMEOUT = 30_000L // 30 seconds
        private const val MAX_HISTORY = 20 // Keep last 20 messages (10 user, 10 model)
    }

}