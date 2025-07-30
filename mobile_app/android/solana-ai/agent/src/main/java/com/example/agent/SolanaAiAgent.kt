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
import com.solana.core.PublicKey
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
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
                // Dispatch to the correct tool based on function name & JSON args
                val toolResult = when (functionName) {
                    // DexScreener
                    "getTokenDataByAddress" ->
                        dexScreenerTools.getTokenDataByAddress(args["mintAddress"]!!.jsonPrimitive.content)

                    "getTokenAddressFromTicker" ->
                        dexScreenerTools.getTokenAddressFromTicker(args["ticker"]!!.jsonPrimitive.content)

                    "getTokenDataByTicker" ->
                        dexScreenerTools.getTokenDataByTicker(args["ticker"]!!.jsonPrimitive.content)

                    // Pyth
                    "fetchPythPriceFeedID" ->
                        pythTools.fetchPythPriceFeedID(args["tokenSymbol"]!!.jsonPrimitive.content)

                    "fetchPythPrice" ->
                        pythTools.fetchPythPrice(args["feedID"]!!.jsonPrimitive.content)

                    "getTokenPriceBySymbol" ->
                        pythTools.getTokenPriceBySymbol(args["tokenSymbol"]!!.jsonPrimitive.content)

                    // Rugcheck
                    "getTokenReportSummary" ->
                        rugcheckTools.getTokenReportSummary(args["mint"]!!.jsonPrimitive.content)

                    "getTokenDetailedReport" ->
                        rugcheckTools.getTokenDetailedReport(args["mint"]!!.jsonPrimitive.content)

                    // Jupiter
                    "stakeWithJup" ->
                        jupiterTools.stakeWithJup(args["amount"]!!.jsonPrimitive.content.toLong())

                    "trade" ->
                        jupiterTools.trade(
                            outputMint = args["outputMint"]!!.jsonPrimitive.content,
                            inputAmount = args["inputAmount"]!!.jsonPrimitive.content.toDouble(),
                            inputMint = args["inputMint"]?.jsonPrimitive?.content.toString()
                        )

                    // Metaplex
                    "createNft" ->
                        metaplexTools.createNft(
                            name = args["name"]!!.jsonPrimitive.content,
                            symbol = args["symbol"]?.jsonPrimitive?.content ?: "",
                            uri = args["uri"]!!.jsonPrimitive.content,
                            sellerFeeBasisPoints = args["sellerFeeBasisPoints"]!!.jsonPrimitive.int,
                            isMutable = args["isMutable"]?.jsonPrimitive?.boolean == true,
                            isCollection = args["isCollection"]?.jsonPrimitive?.boolean == true
                        )

                    "transferNft" ->
                        metaplexTools.transferNft(
                            mintKey = PublicKey.valueOf(args["mintKey"]!!.jsonPrimitive.content),
                            toOwner = PublicKey.valueOf(args["toOwner"]!!.jsonPrimitive.content),
                            amount = args["amount"]?.jsonPrimitive?.int?.toULong() ?: 1UL
                        )

                    "findNftsByOwner" ->
                        metaplexTools.findNftsByOwner(args["ownerAddress"]!!.jsonPrimitive.content)

                    // Helius
                    "createWebhook" ->
                        heliusTools.createWebhook(
                            accountAddresses = args["accountAddresses"]!!.jsonArray.map { it.jsonPrimitive.content },
                            webhookURL = args["webhookURL"]!!.jsonPrimitive.content
                        )

                    "parseTransaction" ->
                        heliusTools.parseTransaction(args["transactionId"]!!.jsonPrimitive.content)

                    "getAssetsByOwner" ->
                        heliusTools.getAssetsByOwner(
                            ownerAddress = args["ownerAddress"]!!.jsonPrimitive.content,
                            limit = args["limit"]!!.jsonPrimitive.int,
                            page = args["page"]?.jsonPrimitive?.int ?: 1,
                            showFungible = args["showFungible"]?.jsonPrimitive?.boolean != false
                        )

                    // CoinGecko
                    "getLatestPrice" ->
                        coinGeckoTools.getLatestPrice()

                    "getTokenInfo" ->
                        coinGeckoTools.getTokenInfo(args["tokenAddress"]!!.jsonPrimitive.content)

                    "getTokenPriceData" ->
                        coinGeckoTools.getTokenPriceData(
                            tokenAddresses = args["tokenAddresses"]!!.jsonArray.map { it.jsonPrimitive.content }
                        )

                    "getTopGainers" ->
                        coinGeckoTools.getTopGainers(
                            duration = args["duration"]?.jsonPrimitive?.content.toString(),
                            topCoins = args["topCoins"]?.jsonPrimitive?.content.toString()
                        )

                    // Allora
                    "getModelPredictions" ->
                        alloraTools.getModelPredictions(args["modelId"]!!.jsonPrimitive.content)

                    "getAvailableModels" ->
                        alloraTools.getAvailableModels()

                    "getModelMetrics" ->
                        alloraTools.getModelMetrics(args["modelId"]!!.jsonPrimitive.content)

                    "getHistoricalPredictions" ->
                        alloraTools.getHistoricalPredictions(
                            modelId = args["modelId"]!!.jsonPrimitive.content,
                            startDate = args["startDate"]!!.jsonPrimitive.content,
                            endDate = args["endDate"]!!.jsonPrimitive.content
                        )

                    // Messari
                    "askMessariAi" ->
                        messariTools.askMessariAi(args["question"]!!.jsonPrimitive.content)

                    // Elfa AI
                    "getSmartMentions" ->
                        elfaAiTools.getSmartMentions(
                            limit = args["limit"]?.jsonPrimitive?.int ?: 100,
                            offset = args["offset"]?.jsonPrimitive?.int ?: 0
                        )

                    "getTopMentionsByTicker" ->
                        elfaAiTools.getTopMentionsByTicker(
                            ticker = args["ticker"]!!.jsonPrimitive.content,
                            timeWindow = args["timeWindow"]?.jsonPrimitive?.content.toString(),
                            page = args["page"]?.jsonPrimitive?.int ?: 1,
                            pageSize = args["pageSize"]?.jsonPrimitive?.int ?: 50,
                            includeAccountDetails = args["includeAccountDetails"]?.jsonPrimitive?.boolean == true
                        )

                    "getTrendingTokens" ->
                        elfaAiTools.getTrendingTokens(
                            timeWindow = args["timeWindow"]?.jsonPrimitive?.content.toString(),
                            page = args["page"]?.jsonPrimitive?.int ?: 1,
                            pageSize = args["pageSize"]?.jsonPrimitive?.int ?: 50,
                            minMentions = args["minMentions"]?.jsonPrimitive?.int ?: 5
                        )

                    // Wallet
                    "createWallet" ->
                        walletTools.createWallet()

                    "sendSOL" ->
                        walletTools.sendSOL(
                            destination = args["destination"]!!.jsonPrimitive.content,
                            amount = args["amount"]!!.jsonPrimitive.int.toLong()
                        )

                    "sendSPLTokens" ->
                        walletTools.sendSPLTokens(
                            mintAddress = args["mintAddress"]!!.jsonPrimitive.content,
                            fromPublicKey = args["fromPublicKey"]!!.jsonPrimitive.content,
                            destinationAddress = args["destinationAddress"]!!.jsonPrimitive.content,
                            amount = args["amount"]!!.jsonPrimitive.int.toLong(),
                            allowUnfundedRecipient = args["allowUnfundedRecipient"]?.jsonPrimitive?.boolean
                                ?: false
                        )

                    // Gibwork
                    "createTask" ->
                        gibworkTools.createTask(
                            title = args["title"]!!.jsonPrimitive.content,
                            content = args["content"]!!.jsonPrimitive.content,
                            requirements = args["requirements"]!!.jsonPrimitive.content,
                            tags = args["tags"]!!.jsonArray.map { it.jsonPrimitive.content },
                            tokenMintAddress = args["tokenMintAddress"]!!.jsonPrimitive.content,
                            tokenAmount = args["tokenAmount"]!!.jsonPrimitive.int.toLong(),
                            activity = activity
                        )

                    "getTaskDetails" ->
                        gibworkTools.getTaskDetails(args["taskId"]!!.jsonPrimitive.content)

                    else -> throw IllegalArgumentException("Unsupported function: $functionName")
                }
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
            val response = ask(message, messageHistoryId)
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