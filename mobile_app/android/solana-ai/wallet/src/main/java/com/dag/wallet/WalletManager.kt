package com.dag.wallet

import Wallet
import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.fragment.app.FragmentActivity
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.KeyPair
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DATA_STORE_FILE_NAME = "wallet_datastore"

// Keep DataStore internal to the module
private val Context.walletPreferencesStore: DataStore<Wallet> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = WalletSerializer
)

class WalletManager(context: Context) : IWalletManager {

    // Internal DataStore access - not exposed to external consumers
    private val dataStore: DataStore<Wallet> = context.walletPreferencesStore

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
                        val keys = listOf(wallet.publicKey) // Assuming single public key

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
        onResult: (List<String>) -> Unit,
        onFailure: (() -> Unit)?
    ) {
        requireBiometricAuth(
            activity = activity,
            onSuccess = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val wallet = dataStore.data.first()
                        val privateKey = wallet.privateKey

                        // TODO: Implement actual signing logic here
                        val signedTransaction = "signed_$txAsBase58"
                        
                        withContext(Dispatchers.Main) {
                            onResult(listOf(signedTransaction))
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
        val sodium = SodiumAndroid()
        val lazySodium = LazySodiumAndroid(sodium)
        val keyPair: KeyPair = lazySodium.cryptoSignKeypair()
        val publicKey = keyPair.publicKey.asHexString
        val privateKey = keyPair.secretKey.asHexString
        val newWallet = Wallet.newBuilder()
            .setPublicKey(publicKey)
            .setPrivateKey(privateKey)
            .setIsRecoveryWallet(false)
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.updateData { newWallet }
        }
    }
}