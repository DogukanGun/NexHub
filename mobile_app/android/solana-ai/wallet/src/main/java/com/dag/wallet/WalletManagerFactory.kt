package com.dag.wallet

import android.content.Context

/**
 * Factory class for creating WalletManager instances.
 * This is the main entry point for external consumers of the wallet module.
 * The DataStore implementation is completely hidden from external users.
 */
object WalletManagerFactory {
    
    /**
     * Creates a new WalletManager instance.
     * 
     * @param context The application context
     * @return A WalletManager instance that handles all wallet operations
     */
    fun create(context: Context): IWalletManager {
        return WalletManager(context)
    }
} 