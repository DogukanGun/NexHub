package com.dag.wallet

import Wallet
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64

object WalletSerializer : Serializer<Wallet> {
    override val defaultValue: Wallet
        get() = Wallet.getDefaultInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun readFrom(input: InputStream): Wallet {
        val encryptedBytes = withContext(Dispatchers.IO) {
            input.use { it.readBytes() }
        }
        if (encryptedBytes.isEmpty()) {
            return defaultValue
        }
        val encryptedBytesDecoded = Base64.getDecoder().decode(encryptedBytes)
        val decryptedBytes = CryptoManager.instance.decrypt(encryptedBytesDecoded)
        return Wallet.parseFrom(decryptedBytes)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun writeTo(t: Wallet, output: OutputStream) {
        val bytes = t.toByteArray()
        val encryptedBytes = CryptoManager.instance.encrypt(bytes)
        val encryptedBytesBase64 = Base64.getEncoder().encode(encryptedBytes)
        withContext(Dispatchers.IO) {
            output.use {
                it.write(encryptedBytesBase64)
            }
        }
    }

}