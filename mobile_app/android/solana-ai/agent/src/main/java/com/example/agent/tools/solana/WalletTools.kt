package com.example.agent.tools.solana

import com.solana.models.Token
import androidx.fragment.app.FragmentActivity
import com.dag.wallet.SolanaWalletManager
import com.solana.core.PublicKey
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

class WalletTools(private val walletManager: SolanaWalletManager) {

    fun createWallet(): String {
        val deferred = CompletableDeferred<String>()
        
        walletManager.createWallet(
            onResult = { publicKey ->
                deferred.complete(publicKey)
            },
            onFailure = {
                deferred.complete("Failed to create wallet")
            }
        )
        
        return runBlocking { deferred.await() }
    }

    fun getPublicKeys(activity: FragmentActivity): String {
        val deferred = CompletableDeferred<String>()
        
        walletManager.getPublicKeys(
            activity = activity,
            onResult = { keys ->
                deferred.complete(keys.joinToString(", "))
            },
            onFailure = {
                deferred.complete("Failed to get public keys")
            }
        )
        
        return runBlocking { deferred.await() }
    }

    fun signTransaction(txAsBase58: String, activity: FragmentActivity): String {
        val deferred = CompletableDeferred<String>()
        
        walletManager.signTransaction(
            txAsBase58 = txAsBase58,
            activity = activity,
            onResult = { signedTx ->
                deferred.complete(signedTx.toString())
            },
            onFailure = {
                deferred.complete("Failed to sign transaction")
            }
        )
        
        return runBlocking { deferred.await() }
    }

    suspend fun getSupportedTokens(): List<Token> {
        return walletManager.getSupportedTokens()
    }

    fun closeTokenAccount(tokenPubkey: String): String {
        val deferred = CompletableDeferred<String>()
        
        walletManager.closeTokenAccount(
            tokenPubkey = PublicKey(tokenPubkey)
        ) { result ->
            result.fold(
                onSuccess = { (signature, _) ->
                    deferred.complete("Successfully closed token account. Signature: $signature")
                },
                onFailure = { error ->
                    deferred.complete("Failed to close token account: ${error.message}")
                }
            )
        }
        
        return runBlocking { deferred.await() }
    }

    fun checkSPLTokenAccountExistence(mintAddress: String, destinationAddress: String): String {
        val deferred = CompletableDeferred<String>()
        
        walletManager.checkSPLTokenAccountExistence(
            mintAddress = PublicKey(mintAddress),
            destinationAddress = PublicKey(destinationAddress)
        ) { result ->
            val res = result.getOrThrows()
            deferred.complete(res.first.toBase58())
        }
        
        return runBlocking { deferred.await() }
    }

    fun getTokenWallets(): String {
        val deferred = CompletableDeferred<String>()
        
        walletManager.getTokenWallets { result ->
            result.fold(
                onSuccess = { wallets ->
                    deferred.complete(wallets.joinToString("\n") { wallet ->
                        "Address: ${wallet.pubkey}, Balance: ${wallet.lamports}"
                    })
                },
                onFailure = { error ->
                    deferred.complete("Failed to get token wallets: ${error.message}")
                }
            )
        }
        
        return runBlocking { deferred.await() }
    }

    fun createTokenAccount(mintAddress: String): String {
        val deferred = CompletableDeferred<String>()
        
        walletManager.createTokenAccount(
            mintAddress = PublicKey(mintAddress)
        ) { result ->
            result.fold(
                onSuccess = { (signature, pubkey) ->
                    deferred.complete("Created token account. Signature: $signature, Address: $pubkey")
                },
                onFailure = { error ->
                    deferred.complete("Failed to create token account: ${error.message}")
                }
            )
        }
        
        return runBlocking { deferred.await() }
    }

    fun sendSOL(destination: String, amount: Long): String {
        val deferred = CompletableDeferred<String>()
        
        walletManager.sendSOL(
            destination = PublicKey(destination),
            amount = amount
        ) { result ->
            result.fold(
                onSuccess = { signature ->
                    deferred.complete("Successfully sent SOL. Signature: $signature")
                },
                onFailure = { error ->
                    deferred.complete("Failed to send SOL: ${error.message}")
                }
            )
        }
        
        return runBlocking { deferred.await() }
    }

    fun sendSPLTokens(
        mintAddress: String,
        fromPublicKey: String,
        destinationAddress: String,
        amount: Long,
        allowUnfundedRecipient: Boolean = false
    ): String {
        val deferred = CompletableDeferred<String>()
        
        walletManager.sendSPLTokens(
            mintAddress = PublicKey(mintAddress),
            fromPublicKey = PublicKey(fromPublicKey),
            destinationAddress = PublicKey(destinationAddress),
            amount = amount,
            allowUnfundedRecipient = allowUnfundedRecipient
        ) { result ->
            result.fold(
                onSuccess = { signature ->
                    deferred.complete("Successfully sent SPL tokens. Signature: $signature")
                },
                onFailure = { error ->
                    deferred.complete("Failed to send SPL tokens: ${error.message}")
                }
            )
        }
        
        return runBlocking { deferred.await() }
    }

    fun saveRecoveryWallet(publicKey: String, activity: FragmentActivity): String {
        val deferred = CompletableDeferred<String>()
        
        walletManager.saveRecoveryWallet(
            publicKey = publicKey,
            activity = activity,
            onResult = { keys ->
                deferred.complete("Successfully saved recovery wallet. Public keys: ${keys.joinToString(", ")}")
            },
            onFailure = {
                deferred.complete("Failed to save recovery wallet")
            }
        )
        
        return runBlocking { deferred.await() }
    }
}