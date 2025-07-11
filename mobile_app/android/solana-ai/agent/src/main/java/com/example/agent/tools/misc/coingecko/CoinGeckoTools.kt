package com.example.agent.tools.misc.coingecko

import com.dag.wallet.BuildConfig
import dev.langchain4j.agent.tool.Tool
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.call.*
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.runBlocking

class CoinGeckoTools {
    val client = HttpClient(CIO)

    @Tool("Returns the latest price of Solana Token (SOL)")
    fun getLatestPrice(): String {
        val url =
            "https://pro-api.coingecko.com/api/v3/onchain/networks/solana/new_pools?include=base_token," +
                    "network&x_cg_pro_api_key=${BuildConfig.COIN_GECKO_KEY}"
        var body: String
        runBlocking {
            val httpResponse: HttpResponse = client.get(url)
            body = httpResponse.body()
        }
        return body
    }

    @Tool("Returns the token info of a given token")
    fun getTokenInfo(tokenAddress: String): String {
        val url =
            "https://pro-api.coingecko.com/api/v3/onchain/networks/solana/tokens/${tokenAddress}/" +
                    "info?x_cg_pro_api_key=${BuildConfig.COIN_GECKO_KEY}"
        var body: String
        runBlocking {
            val httpResponse: HttpResponse = client.get(url)
            body = httpResponse.body()
        }
        return body
    }

    @Tool("Returns price data for multiple tokens including market cap, volume, and 24h change")
    fun getTokenPriceData(tokenAddresses: List<String>): String {
        val joinedAddresses = tokenAddresses.joinToString(",")
        val url = "https://pro-api.coingecko.com/api/v3/simple/token_price/solana" +
                "?contract_addresses=$joinedAddresses" +
                "&vs_currencies=usd" +
                "&include_market_cap=true" +
                "&include_24hr_vol=true" +
                "&include_24hr_change=true" +
                "&include_last_updated_at=true" +
                "&x_cg_pro_api_key=${BuildConfig.COIN_GECKO_KEY}"
        
        var body: String
        runBlocking {
            val httpResponse: HttpResponse = client.get(url)
            body = httpResponse.body()
        }
        return body
    }

    @Tool("Returns top gainers and losers in the market")
    fun getTopGainers(duration: String = "24h", topCoins: String = "all"): String {
        if (BuildConfig.COIN_GECKO_KEY.isEmpty()) {
            throw IllegalStateException("No CoinGecko Pro API key provided")
        }

        val url = "https://pro-api.coingecko.com/api/v3/coins/top_gainers_losers" +
                "?vs_currency=usd" +
                "&duration=$duration" +
                "&top_coins=$topCoins" +
                "&x_cg_pro_api_key=${BuildConfig.COIN_GECKO_KEY}"
        
        var body: String
        runBlocking {
            val httpResponse: HttpResponse = client.get(url)
            body = httpResponse.body()
        }
        return body
    }

    @Tool("Returns trending pools on Solana")
    fun getTrendingPools(duration: String = "24h"): String {
        if (BuildConfig.COIN_GECKO_KEY.isEmpty()) {
            throw IllegalStateException("No CoinGecko Pro API key provided")
        }

        val url = "https://pro-api.coingecko.com/api/v3/onchain/networks/solana/trending_pools" +
                "?include=base_token,network" +
                "&duration=$duration" +
                "&x_cg_pro_api_key=${BuildConfig.COIN_GECKO_KEY}"
        
        var body: String
        runBlocking {
            val httpResponse: HttpResponse = client.get(url)
            body = httpResponse.body()
        }
        return body
    }

    @Tool("Returns trending tokens across all platforms")
    fun getTrendingTokens(): String {
        val url = "https://pro-api.coingecko.com/api/v3/search/trending" +
                "?x_cg_pro_api_key=${BuildConfig.COIN_GECKO_KEY}"
        
        var body: String
        runBlocking {
            val httpResponse: HttpResponse = client.get(url)
            body = httpResponse.body()
        }
        return body
    }
}