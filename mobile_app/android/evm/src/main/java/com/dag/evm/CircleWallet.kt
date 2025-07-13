package com.dag.evm

import android.content.Context
import android.util.Log
import circle.programmablewallet.sdk.WalletSdk

class CircleWallet(
    private val context: Context,
    private val appId: String
) {
    private var walletSdk: WalletSdk? = null
    private var isInitialized = false
    private var isAuthenticated = false

    companion object {
        private const val TAG = "CircleWallet"
        private const val DEFAULT_ENDPOINT = "https://api.circle.com/v1/w3s"
    }

    /**
     * Initialize the Circle Wallet SDK
     * @param endpoint Optional custom API endpoint
     * @param onSuccess Callback for successful initialization
     * @param onError Callback for initialization errors
     */
    fun initialize(
        endpoint: String = DEFAULT_ENDPOINT,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        try {
            WalletSdk.init(
                context,
                config = WalletSdk.Configuration(
                    endPoint = endpoint,
                    appId = appId
                )
            )
            
            isInitialized = true
            Log.d(TAG, "Circle Wallet SDK initialized successfully")
            onSuccess()
        } catch (e: Exception) {
            val errorMessage = "Failed to initialize Circle Wallet: ${e.message}"
            Log.e(TAG, errorMessage)
            onError(errorMessage)
        }
    }

    /**
     * Get the current wallet SDK instance
     * @return WalletSdk instance or null
     */
    fun getSdk(): WalletSdk? {
        return if (isInitialized) walletSdk else null
    }
    
    /**
     * Check if the wallet is initialized
     * @return Boolean indicating initialization status
     */
    fun isWalletInitialized(): Boolean = isInitialized
    
    /**
     * Check if the user is authenticated
     * @return Boolean indicating authentication status
     */
    fun isUserAuthenticated(): Boolean = isAuthenticated
    
    /**
     * Release resources and reset state
     */
    fun release() {
        walletSdk = null
        isInitialized = false
        isAuthenticated = false
        Log.d(TAG, "Circle Wallet resources released")
    }
}