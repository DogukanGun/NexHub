# Solana AI Agent

A comprehensive AI-powered agent for Solana blockchain operations, DeFi, NFTs, and market analysis.

## Features

### 🔐 Wallet Operations
- Create and manage Solana wallets
- Send SOL and SPL tokens
- Check balances and token accounts
- Sign transactions with biometric authentication
- Token account management

### 💰 DeFi Operations
- **Jupiter Exchange**: Token swapping and SOL staking
- **Pyth Network**: Real-time price feeds
- **DexScreener**: Token information and market data
- **Rugcheck**: Security analysis and risk assessment

### 🎨 NFT Operations
- **Metaplex**: NFT creation and minting
- **Candy Machine**: V1 and V2 operations
- NFT transfers and collection management
- Find NFTs by owner, creator, or mint address

### 📊 Market Data & Analytics
- **CoinGecko**: Real-time price data and market trends
- **Messari**: AI-powered crypto insights
- **Elfa AI**: Social sentiment analysis
- **Allora Network**: Predictive analytics
- **Helius**: Transaction monitoring and parsing

### 🛠️ Additional Services
- **Gibwork**: Task creation and management
- Enhanced transaction parsing
- Asset management and portfolio tracking
- Webhook monitoring

## Installation

Add the following dependencies to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("dev.langchain4j:langchain4j-google-ai-gemini:0.36.1")
    implementation("dev.langchain4j:langchain4j-openai:0.36.1")
    implementation("dev.langchain4j:langchain4j:0.36.1")
    
    // Your existing Solana dependencies
    implementation("com.solanamobile:mobile-wallet-adapter-clientlib-ktx:2.0.0")
    // ... other dependencies
}
```

## Quick Start

### 1. Create the Agent

```kotlin
// With Gemini (recommended)
val agent = SolanaAiAgent.createWithGemini(
    apiKey = "your-gemini-api-key",
    walletManager = walletManager,
    activity = this
)

// With OpenAI
val agent = SolanaAiAgent.createWithOpenAI(
    apiKey = "your-openai-api-key",
    walletManager = walletManager,
    activity = this
)
```

### 2. Send Messages

```kotlin
// Basic message
agent.sendMessage(
    message = "What's my wallet balance?",
    onResponse = { response ->
        println("Agent: $response")
    },
    onError = { error ->
        println("Error: $error")
    }
)

// Streaming response
agent.sendMessageStreaming(
    message = "Explain how to stake SOL",
    onToken = { token ->
        print(token) // Real-time token streaming
    },
    onComplete = { fullResponse ->
        println("Complete: $fullResponse")
    },
    onError = { error ->
        println("Error: $error")
    }
)
```

## Usage Examples

### Wallet Operations

```kotlin
// Check balance
agent.sendMessage("What's my current SOL balance?") { response, error ->
    // Handle response
}

// Send tokens
agent.sendMessage("Send 0.1 SOL to [recipient_address]") { response, error ->
    // Handle response
}

// Quick operations
val balance = agent.quickWalletOperation("balance")
val walletInfo = agent.getWalletInfo()
```

### DeFi Operations

```kotlin
// Token swapping
agent.sendMessage("Swap 1 SOL for USDC") { response, error ->
    // Handle swap response
}

// Price checking
agent.sendMessage("What's the current price of SOL?") { response, error ->
    // Handle price response
}

// Security analysis
agent.sendMessage("Is this token safe: [token_address]") { response, error ->
    // Handle security analysis
}

// Staking
agent.sendMessage("How do I stake my SOL?") { response, error ->
    // Handle staking instructions
}
```

### NFT Operations

```kotlin
// View NFTs
agent.sendMessage("Show me my NFTs") { response, error ->
    // Handle NFT list
}

// Create NFT
agent.sendMessage("Help me create an NFT collection") { response, error ->
    // Handle NFT creation guidance
}

// Transfer NFT
agent.sendMessage("Transfer my NFT [mint_address] to [recipient]") { response, error ->
    // Handle NFT transfer
}
```

### Market Analysis

```kotlin
// Market trends
agent.sendMessage("What are the trending tokens today?") { response, error ->
    // Handle market trends
}

// Price analysis
agent.sendMessage("Analyze the price movement of SOL") { response, error ->
    // Handle price analysis
}

// DeFi insights
agent.sendMessage("What's the best yield farming opportunity?") { response, error ->
    // Handle DeFi recommendations
}
```

## Advanced Features

### Chat Memory
The agent maintains conversation context:

```kotlin
// Get chat history
val history = agent.getChatHistory()

// Clear chat history
agent.clearChatHistory()
```

### Health Monitoring

```kotlin
// Check agent health
val healthStatus = agent.healthCheck()
println(healthStatus)
```

### Error Handling

```kotlin
agent.sendMessage("Your message") { response, error ->
    if (error != null) {
        when {
            error.contains("API key") -> {
                // Handle API key issues
            }
            error.contains("network") -> {
                // Handle network issues
            }
            else -> {
                // Handle other errors
            }
        }
    } else {
        // Handle successful response
    }
}
```

## Configuration

### API Keys Required

Set up the following API keys in your `BuildConfig`:

```kotlin
// Required
GEMINI_API_KEY or OPENAI_API_KEY

// Optional (for enhanced features)
COIN_GECKO_KEY
ALLORA_API_KEY
ELFA_AI_API_KEY
HELIUS_API_KEY
MESSARI_API_KEY
```

### Environment Setup

```kotlin
// In your build.gradle.kts
android {
    defaultConfig {
        val geminiKey = System.getenv("GEMINI_API_KEY") ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
        
        val coinGeckoKey = System.getenv("COIN_GECKO_KEY") ?: ""
        buildConfigField("String", "COIN_GECKO_KEY", "\"$coinGeckoKey\"")
        
        // ... other API keys
    }
}
```

## Best Practices

### Security
- Always verify transaction details before signing
- Use biometric authentication for sensitive operations
- Validate addresses and amounts
- Keep API keys secure

### Performance
- Use streaming for long responses
- Implement proper error handling
- Cache frequently accessed data
- Monitor API rate limits

### User Experience
- Provide clear feedback during operations
- Show transaction progress
- Explain complex operations step by step
- Offer confirmation dialogs for financial operations

## Troubleshooting

### Common Issues

1. **API Key Errors**
   ```kotlin
   // Check if API key is properly set
   if (BuildConfig.GEMINI_API_KEY.isEmpty()) {
       // Handle missing API key
   }
   ```

2. **Network Errors**
   ```kotlin
   // Implement retry logic
   agent.sendMessage(message, maxRetries = 3) { response, error ->
       // Handle response
   }
   ```

3. **Wallet Connection Issues**
   ```kotlin
   // Verify wallet connection
   val walletInfo = agent.getWalletInfo()
   if (walletInfo.contains("Error")) {
       // Handle wallet issues
   }
   ```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add your improvements
4. Write tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue on GitHub
- Check the documentation
- Review the example code

## Changelog

### v1.0.0
- Initial release
- Full Solana wallet integration
- DeFi operations support
- NFT management
- Market data integration
- AI-powered insights

---

**Note**: This is a powerful tool that handles real cryptocurrency operations. Always test thoroughly in a development environment before using with real funds. 