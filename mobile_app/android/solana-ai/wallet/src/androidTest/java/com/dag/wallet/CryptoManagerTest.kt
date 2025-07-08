package com.dag.wallet

import org.junit.Assert
import org.junit.Test

class CryptoManagerTest {
    @Test
    fun encryption_works_as_expected(){
        val cryptoManager = CryptoManager()
        val messageToEncrypt = "test_encryption_works_as_expected"
        val encryptedResult = cryptoManager.encrypt(messageToEncrypt.toByteArray())
        Assert.assertNotNull(encryptedResult)
        val decryptedResult = cryptoManager.decrypt(encryptedResult)
        Assert.assertEquals(messageToEncrypt, String(decryptedResult))
    }
}