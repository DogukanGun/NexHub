package com.dag.wallet

import androidx.fragment.app.FragmentActivity


interface IWalletManager {
    fun getPublicKeys(
        activity: FragmentActivity,
        onResult: (List<String>) -> Unit,
        onFailure: (() -> Unit)? = null
    )
    fun signTransaction(
        txAsBase58: String,
        activity: FragmentActivity,
        onResult: (List<String>) -> Unit,
        onFailure: (() -> Unit)? = null
    )
    fun saveRecoveryWallet(
        publicKey: String,
        activity: FragmentActivity,
        onResult: (List<String>) -> Unit,
        onFailure: (() -> Unit)? = null
    )
    fun createWallet(
        onResult: (String) -> Unit,
        onFailure: (() -> Unit)? = null
    )
}