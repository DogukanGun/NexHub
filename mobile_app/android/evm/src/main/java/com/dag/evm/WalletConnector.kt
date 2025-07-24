package com.dag.evm

import android.content.Context
import io.metamask.androidsdk.DappMetadata
import io.metamask.androidsdk.Ethereum
import io.metamask.androidsdk.EthereumRequest
import io.metamask.androidsdk.SDKOptions
import io.metamask.androidsdk.Result
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.TypeReference

class WalletConnector(
    context: Context,
    infuraKey: String
) {
    val metadata = DappMetadata("NexHub", "https://www.nexarb.com")
    val readonlyRPCMap = mapOf("0x38" to "https://bnb-mainnet.g.alchemy.com/v2/$infuraKey")
    val ethereum = Ethereum(context, metadata, SDKOptions(infuraKey, readonlyRPCMap))
    
    // Connect to wallet
    fun connect(callback: ((Result) -> Unit)?) {
        ethereum.connect(callback)
    }

    // Call all RPC methods
    fun sendRequest(request: EthereumRequest, callback: ((Result) -> Unit)?) {
        ethereum.sendRequest(request, callback)
    }

    fun encodeGenerateContractsCall(message: String, githubRepo: String): String {
        val function = Function(
            "generateContracts",
            listOf(Utf8String(message), Utf8String(githubRepo)),
            emptyList<TypeReference<*>>()
        )
        return FunctionEncoder.encode(function)
    }
    
    // Call smart contract generator function
    fun callSmartContractGenerator(
        message: String,
        githubRepo: String
    ){
        val contractAddress = "0x..."
        val data = encodeGenerateContractsCall(message, githubRepo)

        val params = listOf(
            mapOf(
                "from" to ethereum.selectedAddress,
                "to" to contractAddress,
                "data" to data,
            ),
            "latest"
        )

        ethereum.sendRequest(EthereumRequest(
            method = "eth_sendTransaction",
            params = params
        ))
    }

}