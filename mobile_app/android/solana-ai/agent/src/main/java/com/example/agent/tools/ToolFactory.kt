package com.example.agent.tools

import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool

@OptIn(PublicPreviewAPI::class)
fun createDexScreenerTools(): List<Tool> {
    val getTokenDataByAddressTool = FunctionDeclaration(
        name = "getTokenDataByAddress",
        description = "Get token data by mint address",
        parameters = mapOf(
            "mintAddress" to Schema.string("Mint address of the token")
        )
    )
    val getTokenAddressFromTickerTool = FunctionDeclaration(
        name = "getTokenAddressFromTicker",
        description = "Get token address from ticker symbol",
        parameters = mapOf(
            "ticker" to Schema.string("Token ticker symbol")
        )
    )
    val getTokenDataByTickerTool = FunctionDeclaration(
        name = "getTokenDataByTicker",
        description = "Get token data by ticker symbol",
        parameters = mapOf(
            "ticker" to Schema.string("Token ticker symbol")
        )
    )
    val functionDeclarationList = listOf(
        getTokenDataByAddressTool,
        getTokenAddressFromTickerTool,
        getTokenDataByTickerTool
    )
    return listOf(Tool.functionDeclarations(functionDeclarationList))
}

@OptIn(PublicPreviewAPI::class)
fun createPythTools(): List<Tool> {
    val fetchPythPriceFeedIDTool = FunctionDeclaration(
        name = "fetchPythPriceFeedID",
        description = "Fetch Pyth price feed ID for a token symbol",
        parameters = mapOf(
            "tokenSymbol" to Schema.string("Token symbol to fetch price feed ID")
        )
    )
    val fetchPythPriceTool = FunctionDeclaration(
        name = "fetchPythPrice",
        description = "Fetch Pyth price for a given feed ID",
        parameters = mapOf(
            "feedID" to Schema.string("Price feed ID")
        )
    )
    val getTokenPriceBySymbolTool = FunctionDeclaration(
        name = "getTokenPriceBySymbol",
        description = "Get token price by symbol",
        parameters = mapOf(
            "tokenSymbol" to Schema.string("Token symbol to fetch price")
        )
    )
    val functionDeclarationList = listOf(
        fetchPythPriceFeedIDTool,
        fetchPythPriceTool,
        getTokenPriceBySymbolTool
    )
    return listOf(Tool.functionDeclarations(functionDeclarationList))
}

@OptIn(PublicPreviewAPI::class)
fun createRugCheckTools(): List<Tool> {
    val getTokenReportSummaryTool = FunctionDeclaration(
        name = "getTokenReportSummary",
        description = "Get token report summary by mint address",
        parameters = mapOf(
            "mint" to Schema.string("Token mint address")
        )
    )
    val getTokenDetailedReportTool = FunctionDeclaration(
        name = "getTokenDetailedReport",
        description = "Get detailed token report by mint address",
        parameters = mapOf(
            "mint" to Schema.string("Token mint address")
        )
    )
    val functionDeclarationList = listOf(
        getTokenReportSummaryTool,
        getTokenDetailedReportTool
    )
    return listOf(Tool.functionDeclarations(functionDeclarationList))
}

@OptIn(PublicPreviewAPI::class)
fun createJupiterTools(): List<Tool> {
    val stakeWithJupTool = FunctionDeclaration(
        name = "stakeWithJup",
        description = "Stake SOL using Jupiter",
        parameters = mapOf(
            "amount" to Schema.integer("Amount of SOL to stake")
        )
    )
    val tradeTool = FunctionDeclaration(
        name = "trade",
        description = "Execute token trade on Jupiter",
        parameters = mapOf(
            "outputMint" to Schema.string("Output token mint address"),
            "inputAmount" to Schema.integer("Input amount to trade"),
            "inputMint" to Schema.string("Input token mint address (optional, defaults to USDC)")
        )
    )
    val functionDeclarationList = listOf(
        stakeWithJupTool,
        tradeTool
    )
    return listOf(Tool.functionDeclarations(functionDeclarationList))
}

@OptIn(PublicPreviewAPI::class)
fun createMetaplexTools(): List<Tool> {
    val createNftTool = FunctionDeclaration(
        name = "createNft",
        description = "Create a new NFT",
        parameters = mapOf(
            "name" to Schema.string("NFT name"),
            "symbol" to Schema.string("NFT symbol (optional)"),
            "uri" to Schema.string("NFT metadata URI"),
            "sellerFeeBasisPoints" to Schema.integer("Seller fee basis points"),
            "isMutable" to Schema.boolean("Whether the NFT is mutable (optional)"),
            "isCollection" to Schema.boolean("Whether the NFT is a collection (optional)")
        )
    )
    val transferNftTool = FunctionDeclaration(
        name = "transferNft",
        description = "Transfer an NFT",
        parameters = mapOf(
            "mintKey" to Schema.string("NFT mint key/address"),
            "toOwner" to Schema.string("Recipient's public key"),
            "amount" to Schema.integer("Amount of NFTs to transfer (optional, default 1)")
        )
    )
    val findNftsByOwnerTool = FunctionDeclaration(
        name = "findNftsByOwner",
        description = "Find NFTs owned by a specific address",
        parameters = mapOf(
            "ownerAddress" to Schema.string("Owner's public key")
        )
    )
    val functionDeclarationList = listOf(
        createNftTool,
        transferNftTool,
        findNftsByOwnerTool
    )
    return listOf(Tool.functionDeclarations(functionDeclarationList))
}

@OptIn(PublicPreviewAPI::class)
fun createHeliusTools(): List<Tool> {
    val createWebhookTool = FunctionDeclaration(
        name = "createWebhook",
        description = "Create a webhook for tracking transactions",
        parameters = mapOf(
            "accountAddresses" to Schema.array(
                items = Schema.string("Account address"),
                description = "List of account addresses to track"
            ),
            "webhookURL" to Schema.string("URL to receive webhook notifications")
        )
    )
    val parseTransactionTool = FunctionDeclaration(
        name = "parseTransaction",
        description = "Parse a transaction by its ID",
        parameters = mapOf(
            "transactionId" to Schema.string("Transaction ID to parse")
        )
    )
    val getAssetsByOwnerTool = FunctionDeclaration(
        name = "getAssetsByOwner",
        description = "Get assets owned by a specific address",
        parameters = mapOf(
            "ownerAddress" to Schema.string("Owner's public key"),
            "limit" to Schema.integer("Number of assets to retrieve"),
            "page" to Schema.integer("Page number for pagination (optional)"),
            "showFungible" to Schema.boolean("Whether to show fungible tokens (optional)")
        )
    )
    val functionDeclarationList = listOf(
        createWebhookTool,
        parseTransactionTool,
        getAssetsByOwnerTool
    )
    return listOf(Tool.functionDeclarations(functionDeclarationList))
}

@OptIn(PublicPreviewAPI::class)
fun createCoinGeckoTools(): List<Tool> {
    val getLatestPriceTool = FunctionDeclaration(
        name = "getLatestPrice",
        description = "Get latest price information",
        parameters = emptyMap()
    )
    val getTokenInfoTool = FunctionDeclaration(
        name = "getTokenInfo",
        description = "Get token information by address",
        parameters = mapOf(
            "tokenAddress" to Schema.string("Token contract address")
        )
    )
    val getTokenPriceDataTool = FunctionDeclaration(
        name = "getTokenPriceData",
        description = "Get price data for multiple token addresses",
        parameters = mapOf(
            "tokenAddresses" to Schema.array(
                items = Schema.string("Token contract address"),
                description = "List of token addresses"
            )
        )
    )
    val getTopGainersTool = FunctionDeclaration(
        name = "getTopGainers",
        description = "Get top gaining tokens",
        parameters = mapOf(
            "duration" to Schema.string("Time duration for gains (optional)"),
            "topCoins" to Schema.string("Number or type of top coins (optional)")
        )
    )
    val functionDeclarationList = listOf(
        getLatestPriceTool,
        getTokenInfoTool,
        getTokenPriceDataTool,
        getTopGainersTool
    )
    return listOf(Tool.functionDeclarations(functionDeclarationList))
}

@OptIn(PublicPreviewAPI::class)
fun createAlloraTools(): List<Tool> {
    val getModelPredictionsTool = FunctionDeclaration(
        name = "getModelPredictions",
        description = "Get predictions for a specific model",
        parameters = mapOf(
            "modelId" to Schema.string("ID of the model to get predictions")
        )
    )
    val getAvailableModelsTool = FunctionDeclaration(
        name = "getAvailableModels",
        description = "Get list of available models",
        parameters = emptyMap()
    )
    val getModelMetricsTool = FunctionDeclaration(
        name = "getModelMetrics",
        description = "Get metrics for a specific model",
        parameters = mapOf(
            "modelId" to Schema.string("ID of the model to get metrics")
        )
    )
    val getHistoricalPredictionsTool = FunctionDeclaration(
        name = "getHistoricalPredictions",
        description = "Get historical predictions for a model within a date range",
        parameters = mapOf(
            "modelId" to Schema.string("ID of the model"),
            "startDate" to Schema.string("Start date for historical predictions"),
            "endDate" to Schema.string("End date for historical predictions")
        )
    )
    val functionDeclarationList = listOf(
        getModelPredictionsTool,
        getAvailableModelsTool,
        getModelMetricsTool,
        getHistoricalPredictionsTool
    )
    return listOf(Tool.functionDeclarations(functionDeclarationList))
}

@OptIn(PublicPreviewAPI::class)
fun createMessariTools(): List<Tool> {
    val askMessariAiTool = FunctionDeclaration(
        name = "askMessariAi",
        description = "Ask a question to Messari AI",
        parameters = mapOf(
            "question" to Schema.string("Question to ask Messari AI")
        )
    )
    val functionDeclarationList = listOf(askMessariAiTool)
    return listOf(Tool.functionDeclarations(functionDeclarationList))
}

@OptIn(PublicPreviewAPI::class)
fun createElfaAiTools(): List<Tool> {
    val getSmartMentionsTool = FunctionDeclaration(
        name = "getSmartMentions",
        description = "Get smart mentions",
        parameters = mapOf(
            "limit" to Schema.integer("Number of mentions to retrieve (optional)"),
            "offset" to Schema.integer("Offset for pagination (optional)")
        )
    )
    val getTopMentionsByTickerTool = FunctionDeclaration(
        name = "getTopMentionsByTicker",
        description = "Get top mentions for a specific ticker",
        parameters = mapOf(
            "ticker" to Schema.string("Token ticker symbol"),
            "timeWindow" to Schema.string("Time window for mentions (optional)"),
            "page" to Schema.integer("Page number (optional)"),
            "pageSize" to Schema.integer("Number of results per page (optional)"),
            "includeAccountDetails" to Schema.boolean("Whether to include account details (optional)")
        )
    )
    val getTrendingTokensTool = FunctionDeclaration(
        name = "getTrendingTokens",
        description = "Get trending tokens",
        parameters = mapOf(
            "timeWindow" to Schema.string("Time window for trending tokens (optional)"),
            "page" to Schema.integer("Page number (optional)"),
            "pageSize" to Schema.integer("Number of results per page (optional)"),
            "minMentions" to Schema.integer("Minimum number of mentions (optional)")
        )
    )
    val functionDeclarationList = listOf(
        getSmartMentionsTool,
        getTopMentionsByTickerTool,
        getTrendingTokensTool
    )
    return listOf(Tool.functionDeclarations(functionDeclarationList))
}

@OptIn(PublicPreviewAPI::class)
fun createWalletTools(): List<Tool> {
    val createWalletTool = FunctionDeclaration(
        name = "createWallet",
        description = "Create a new wallet",
        parameters = emptyMap()
    )
    val sendSOLTool = FunctionDeclaration(
        name = "sendSOL",
        description = "Send SOL to a destination address",
        parameters = mapOf(
            "destination" to Schema.string("Destination wallet address"),
            "amount" to Schema.integer("Amount of SOL to send")
        )
    )
    val sendSPLTokensTool = FunctionDeclaration(
        name = "sendSPLTokens",
        description = "Send SPL tokens to a destination address",
        parameters = mapOf(
            "mintAddress" to Schema.string("Token mint address"),
            "fromPublicKey" to Schema.string("Sender's public key"),
            "destinationAddress" to Schema.string("Recipient's address"),
            "amount" to Schema.integer("Amount of tokens to send"),
            "allowUnfundedRecipient" to Schema.boolean("Allow sending to unfunded recipient (optional)")
        )
    )
    val functionDeclarationList = listOf(
        createWalletTool,
        sendSOLTool,
        sendSPLTokensTool
    )
    return listOf(Tool.functionDeclarations(functionDeclarationList))
}

@OptIn(PublicPreviewAPI::class)
fun createGibworkTools(): List<Tool> {
    val createTaskTool = FunctionDeclaration(
        name = "createTask",
        description = "Create a new task with token reward",
        parameters = mapOf(
            "title" to Schema.string("Task title"),
            "content" to Schema.string("Task content"),
            "requirements" to Schema.string("Task requirements"),
            "tags" to Schema.array(
                items = Schema.string("Task tag"),
                description = "List of task tags"
            ),
            "tokenMintAddress" to Schema.string("Token mint address for reward"),
            "tokenAmount" to Schema.integer("Amount of tokens for reward")
        )
    )
    val getTaskDetailsTool = FunctionDeclaration(
        name = "getTaskDetails",
        description = "Get details of a specific task",
        parameters = mapOf(
            "taskId" to Schema.string("ID of the task")
        )
    )
    val functionDeclarationList = listOf(
        createTaskTool,
        getTaskDetailsTool
    )
    return listOf(Tool.functionDeclarations(functionDeclarationList))
}