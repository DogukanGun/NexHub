package com.dag.wallet

import Wallet
import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.fragment.app.FragmentActivity
import com.solana.Solana
import com.solana.actions.SPLTokenDestinationAddress
import com.solana.actions.checkSPLTokenAccountExistence
import com.solana.actions.closeTokenAccount
import com.solana.actions.createTokenAccount
import com.solana.actions.findSPLTokenDestinationAddress
import com.solana.actions.getMintData
import com.solana.actions.getTokenWallets
import com.solana.actions.sendSOL
import com.solana.actions.sendSPLTokens
import com.solana.actions.serializeAndSendWithFee
import com.solana.core.Account
import com.solana.core.DerivationPath
import com.solana.core.HotAccount
import com.solana.core.PublicKey
import com.solana.core.Transaction
import com.solana.models.Token
import com.solana.models.buffer.Mint
import com.solana.networking.HttpNetworkingRouter
import com.solana.networking.RPCEndpoint
import com.solana.vendor.Result
import com.solana.vendor.ResultError
import com.solana.vendor.bip39.Mnemonic
import com.solana.vendor.bip39.WordCount
import foundation.metaplex.solana.transactions.SolanaTransaction
import foundation.metaplex.solanaeddsa.SolanaEddsa
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

private const val DATA_STORE_FILE_NAME = "wallet_datastore"

// Keep DataStore internal to the module
private val Context.walletPreferencesStore: DataStore<Wallet> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = WalletSerializer
)

class SolanaWalletManager(context: Context) : IWalletManager, ISolanaWalletManager {

    // Internal DataStore access - not exposed to external consumers
    private val dataStore: DataStore<Wallet> = context.walletPreferencesStore

    override fun getSolanaRpc(): Solana {
        var rpcEndpoint = RPCEndpoint.mainnetBetaSolana
        if (BuildConfig.DEBUG) {
            rpcEndpoint = RPCEndpoint.mainnetBetaSolana
        }
        return Solana(HttpNetworkingRouter(rpcEndpoint))
    }

    private suspend fun getAccount(): HotAccount {
        val wallet = dataStore.data.first()
        val phrase24 = wallet.privateKey
        return HotAccount.fromMnemonic(phrase24.split(","), "", DerivationPath.DEPRECATED_M_501H_0H_0_0)
    }

    private fun requireBiometricAuth(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: (() -> Unit)? = null
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication Required")
            .setSubtitle("Please authenticate to proceed")
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onFailure?.invoke()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(activity, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    override fun getPublicKeys(
        activity: FragmentActivity,
        onResult: (List<String>) -> Unit,
        onFailure: (() -> Unit)?
    ) {
        requireBiometricAuth(
            activity = activity,
            onSuccess = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val wallet = dataStore.data.first()
                        val keys = listOf(wallet.publicKey)
                        withContext(Dispatchers.Main) {
                            onResult(keys)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            onFailure?.invoke()
                        }
                    }
                }
            },
            onFailure = {
                onFailure?.invoke()
            }
        )
    }

    override fun signTransaction(
        txAsBase58: String,
        activity: FragmentActivity,
        onResult: (ByteArray) -> Unit,
        onFailure: (() -> Unit)?
    ) {
        requireBiometricAuth(
            activity = activity,
            onSuccess = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val wallet = dataStore.data.first()
                        val phrase24 = wallet.privateKey
                        val account = HotAccount.fromMnemonic(phrase24.split(","), "", DerivationPath.DEPRECATED_M_501H_0H_0_0)
                        val transaction = SolanaTransaction.from(txAsBase58.toByteArray())
                        val signedTx = account.sign(transaction.serializeMessage())
                        withContext(Dispatchers.Main) {
                            onResult(signedTx)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            onFailure?.invoke()
                        }
                    }
                }
            },
            onFailure = {
                onFailure?.invoke()
            }
        )
    }

    override fun saveRecoveryWallet(
        publicKey: String,
        activity: FragmentActivity,
        onResult: (List<String>) -> Unit,
        onFailure: (() -> Unit)?
    ) {
        requireBiometricAuth(
            activity = activity,
            onSuccess = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val newWallet = Wallet.newBuilder()
                            .setPublicKey(publicKey)
                            .setIsRecoveryWallet(true)
                            .build()
                        
                        dataStore.updateData { newWallet }
                        
                        withContext(Dispatchers.Main) {
                            onResult(listOf(publicKey))
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            onFailure?.invoke()
                        }
                    }
                }
            },
            onFailure = {
                onFailure?.invoke()
            }
        )
    }

    override fun createWallet(
        onResult: (String) -> Unit,
        onFailure: (() -> Unit)?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val keypair = SolanaEddsa.generateKeypair()
            val publicKey = keypair.publicKey.toBase58()
            val phrase24 = Mnemonic(WordCount.COUNT_24).phrase
            val privateKey = phrase24.joinToString()
            val newWallet = Wallet.newBuilder()
                .setPublicKey(publicKey)
                .setPrivateKey(privateKey)
                .setIsRecoveryWallet(false)
                .build()
            dataStore.updateData { newWallet }
        }
    }

    override fun getPublicKey(): Account {
        lateinit var account: HotAccount
        runBlocking {
            account = getAccount()
        }
        return account
    }

    override fun getSupportedTokens(): List<Token> {
        val rpc = getSolanaRpc()
        return rpc.supportedTokens
    }

    override fun closeTokenAccount(
        tokenPubkey: PublicKey,
        onComplete: (kotlin.Result<Pair<String, PublicKey>>) -> Unit
    ) {
        val rpc = getSolanaRpc()
        CoroutineScope(Dispatchers.IO).launch {
            val account = getAccount()
            rpc.action.closeTokenAccount(
                account,
                tokenPubkey,
                onComplete
            )
        }
    }

    override fun checkSPLTokenAccountExistence(
        mintAddress: PublicKey,
        destinationAddress: PublicKey,
        onComplete: (Result<SPLTokenDestinationAddress, ResultError>) -> Unit
    ) {
        val rpc = getSolanaRpc()
        CoroutineScope(Dispatchers.IO).launch {
            val res = rpc.action.checkSPLTokenAccountExistence(
                mintAddress,
                destinationAddress,
            )
            onComplete(res)
        }
    }

    override fun getTokenWallets(onComplete: (kotlin.Result<List<com.solana.models.Wallet>>) -> Unit) {
        val rpc = getSolanaRpc()
        CoroutineScope(Dispatchers.IO).launch {
            val account = getAccount()
            rpc.action.getTokenWallets(
                account.publicKey,
                onComplete
            )
        }
    }

    override fun createTokenAccount(
        mintAddress: PublicKey,
        onComplete: (kotlin.Result<Pair<String, PublicKey>>) -> Unit
    ) {
        val rpc = getSolanaRpc()
        CoroutineScope(Dispatchers.IO).launch {
            val account = getAccount()
            rpc.action.createTokenAccount(account,mintAddress,onComplete)
        }
    }

    override fun getMintData(
        mintAddress: PublicKey,
        programId: PublicKey,
        onComplete: (kotlin.Result<Mint>) -> Unit
    ) {
        val rpc = getSolanaRpc()
        CoroutineScope(Dispatchers.IO).launch {
            rpc.action.getMintData(mintAddress,programId,onComplete)
        }
    }

    override fun findSPLTokenDestinationAddress(
        mintAddress: PublicKey,
        destinationAddress: PublicKey,
        allowUnfundedRecipient: Boolean,
        onComplete: (Result<SPLTokenDestinationAddress, ResultError>) -> Unit
    ) {
        val rpc = getSolanaRpc()
        CoroutineScope(Dispatchers.IO).launch {
            rpc.action.findSPLTokenDestinationAddress(mintAddress,destinationAddress,allowUnfundedRecipient,onComplete)
        }
    }

    override fun sendSOL(
        destination: PublicKey,
        amount: Long,
        onComplete: (kotlin.Result<String>) -> Unit
    ) {
        val rpc = getSolanaRpc()
        CoroutineScope(Dispatchers.IO).launch {
            val account = getAccount()
            rpc.action.sendSOL(account,destination,amount,onComplete)
        }
    }

    override fun sendSPLTokens(
        mintAddress: PublicKey,
        fromPublicKey: PublicKey,
        destinationAddress: PublicKey,
        amount: Long,
        allowUnfundedRecipient: Boolean,
        onComplete: (kotlin.Result<String>) -> Unit
    ) {
        val rpc = getSolanaRpc()
        CoroutineScope(Dispatchers.IO).launch {
            val account = getAccount()
            val res = rpc.action.sendSPLTokens(mintAddress,fromPublicKey,destinationAddress,amount,allowUnfundedRecipient,account)
            onComplete(res)
        }
    }

    override fun serializeAndSendWithFee(
        transaction: Transaction,
        recentBlockHash: String?,
        onComplete: (kotlin.Result<String>) -> Unit
    ) {
        val rpc = getSolanaRpc()
        CoroutineScope(Dispatchers.IO).launch {
            val account = getAccount()
            rpc.action.serializeAndSendWithFee(transaction,listOf(account),recentBlockHash,onComplete)
        }
    }
}