package com.dag.wallet

import android.content.Context

/**
 * Factory class for creating SolanaWalletManager instances.
 * This is the main entry point for external consumers of the wallet module.
 * The DataStore implementation is completely hidden from external users.
 */
object WalletManagerFactory {
    
    /**
     * Creates a new SolanaWalletManager instance.
     * 
     * @param context The application context
     * @return A SolanaWalletManager instance that handles all wallet operations
     */
    fun createSolanaWallet(context: Context): IWalletManager {
        return SolanaWalletManager(context)
    }
} 