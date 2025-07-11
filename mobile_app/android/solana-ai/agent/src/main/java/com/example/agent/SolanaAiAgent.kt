package com.example.agent

import androidx.fragment.app.FragmentActivity
import com.dag.aiagent.AiAgent
import com.dag.aiagent.AiModel
import com.dag.wallet.SolanaWalletManager
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
import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.output.Response
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.SystemMessage as SystemMessageAnnotation
import dev.langchain4j.service.UserMessage as UserMessageAnnotation
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

class SolanaAiAgent(
    model: AiModel,
    private val walletManager: SolanaWalletManager,
    private val activity: FragmentActivity
) : AiAgent(model) {

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

    // Chat memory for maintaining conversation context
    private val chatMemory = MessageWindowChatMemory.withMaxMessages(20)

    // AI Service interface for tool integration
    interface SolanaAssistant {
        fun chat(
            @UserMessageAnnotation message: String
        ): String
    }

    // Create AI service with all tools using the same model configuration as parent
    private val aiService = AiServices.builder(SolanaAssistant::class.java)
        .chatLanguageModel(when (model) {
            is AiModel.Gemini -> GoogleAiGeminiChatModel.builder()
                .apiKey(model.apiKey)
                .modelName("gemini-1.5-pro")
                .build()
            is AiModel.OpenAI -> OpenAiChatModel.builder()
                .apiKey(model.apiKey)
                .build()
        })
        .chatMemory(chatMemory)
        .tools(
            // Wallet tools
            walletTools,
            // DeFi tools
            jupiterTools,
            dexScreenerTools,
            pythTools,
            rugcheckTools,
            // NFT tools
            metaplexTools,
            // Misc tools
            coinGeckoTools,
            alloraTools,
            elfaAiTools,
            gibworkTools,
            heliusTools,
            messariTools
        )
        .build()

    // Initialize system message in chat memory
    init {
        chatMemory.add(SystemMessage.from("""
            You are a Solana AI Agent specialized in blockchain operations, DeFi, and NFTs.
            
            Your capabilities include:
            
            🔐 WALLET OPERATIONS:
            - Create and manage Solana wallets
            - Send SOL and SPL tokens
            - Check balances and token accounts
            - Sign transactions with biometric authentication
            
            💰 DEFI OPERATIONS:
            - Token swapping via Jupiter Exchange
            - Price feeds from Pyth Network
            - Token information from DexScreener
            - Security analysis via Rugcheck
            - SOL staking with Jupiter validator
            
            🎨 NFT OPERATIONS:
            - Create and mint NFTs using Metaplex
            - Candy Machine operations (V1 and V2)
            - NFT transfers and collection management
            - Find NFTs by owner, creator, or mint address
            
            📊 MARKET DATA:
            - Real-time price data from CoinGecko
            - Token trends and market analysis
            - AI-powered insights from Messari
            - Social sentiment from Elfa AI
            - Predictive analytics from Allora Network
            
            🛠️ ADDITIONAL SERVICES:
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
        """.trimIndent()))
    }

    /**
     * Send a message to the Solana AI Agent and get a response
     * @param message The user's message
     * @param onResponse Callback for when response is received
     * @param onError Callback for when an error occurs
     */
    fun sendMessage(
        message: String,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Process the message through AI service
            val response = aiService.chat(message)
            onResponse(response)
            
        } catch (e: Exception) {
            onError("Error processing message: ${e.message}")
        }
    }

    /**
     * Send a message with streaming response using inherited function
     * @param message The user's message
     * @param onToken Callback for each token received
     * @param onComplete Callback when response is complete
     * @param onError Callback for errors
     */
    fun sendMessageStreaming(
        message: String,
        onToken: (String) -> Unit,
        onComplete: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val messages = mutableListOf<ChatMessage>()
            
            // Add conversation history (includes system message from init)
            messages.addAll(chatMemory.messages())
            messages.add(UserMessage.from(message))

            // Use inherited streaming generation function
            super.generate(
                messages = messages,
                toolSpecifications = mutableListOf(), // Tools are handled by AiServices
                onNext = onToken,
                onComplete = { response ->
                    val responseText = response.content().text()
                    chatMemory.add(UserMessage.from(message))
                    chatMemory.add(response.content())
                    onComplete(responseText)
                },
                onError = { error ->
                    onError("Streaming error: ${error.message}")
                }
            )
        } catch (e: Exception) {
            onError("Error in streaming: ${e.message}")
        }
    }

    /**
     * Send a message with non-streaming response using inherited function
     * @param message The user's message
     * @param onComplete Callback when response is complete
     * @param onError Callback for errors
     */
    fun sendMessageNonStreaming(
        message: String,
        onComplete: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val messages = mutableListOf<ChatMessage>()
            
            // Add conversation history (includes system message from init)
            messages.addAll(chatMemory.messages())
            messages.add(UserMessage.from(message))

            // Use inherited non-streaming generation function
            super.generateNonStreaming(
                messages = messages,
                toolSpecifications = mutableListOf(), // Tools are handled by AiServices
                onComplete = { response ->
                    val responseText = response.content().text()
                    chatMemory.add(UserMessage.from(message))
                    chatMemory.add(response.content())
                    onComplete(responseText)
                },
                onError = { error ->
                    onError("Non-streaming error: ${error.message}")
                }
            )
        } catch (e: Exception) {
            onError("Error in non-streaming: ${e.message}")
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
     * Clear chat memory
     */
    fun clearChatHistory() {
        chatMemory.clear()
    }

    /**
     * Get chat history
     */
    fun getChatHistory(): List<ChatMessage> {
        return chatMemory.messages()
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

    /**
     * Health check for all services
     */
    fun healthCheck(): String {
        return buildString {
            appendLine("Solana AI Agent Health Check:")
            appendLine("✅ Wallet Manager: Connected")
            appendLine("✅ AI Model: ${if (model is AiModel.Gemini) "Gemini" else "OpenAI"}")
            appendLine("✅ Tools Loaded: ${getAllAvailableTools().size}")
            appendLine("✅ Chat Memory: ${chatMemory.messages().size} messages")
            appendLine("✅ Network: Solana Mainnet")
        }
    }

    /**
     * Direct tool access for advanced usage
     */
    fun getWalletTools() = walletTools
    fun getJupiterTools() = jupiterTools
    fun getDexScreenerTools() = dexScreenerTools
    fun getPythTools() = pythTools
    fun getRugcheckTools() = rugcheckTools
    fun getMetaplexTools() = metaplexTools
    fun getCoinGeckoTools() = coinGeckoTools
    fun getAlloraTools() = alloraTools
    fun getElfaAiTools() = elfaAiTools
    fun getGibworkTools() = gibworkTools
    fun getHeliusTools() = heliusTools
    fun getMessariTools() = messariTools

    companion object {
        const val MAX_RETRIES = 3
        const val TIMEOUT_SECONDS = 30L
        
        /**
         * Create a Solana AI Agent with Gemini
         */
        fun createWithGemini(
            apiKey: String,
            walletManager: SolanaWalletManager,
            activity: FragmentActivity
        ): SolanaAiAgent {
            return SolanaAiAgent(
                AiModel.Gemini(apiKey),
                walletManager,
                activity
            )
        }

        /**
         * Create a Solana AI Agent with OpenAI
         */
        fun createWithOpenAI(
            apiKey: String,
            walletManager: SolanaWalletManager,
            activity: FragmentActivity
        ): SolanaAiAgent {
            return SolanaAiAgent(
                AiModel.OpenAI(apiKey),
                walletManager,
                activity
            )
        }
    }
}