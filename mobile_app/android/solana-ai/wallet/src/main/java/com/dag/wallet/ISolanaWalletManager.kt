package com.dag.wallet

import com.solana.Solana
import com.solana.actions.SPLTokenDestinationAddress
import com.solana.core.Account
import com.solana.core.PublicKey
import com.solana.core.Transaction
import com.solana.models.Token
import com.solana.models.Wallet
import com.solana.models.buffer.Mint
import com.solana.programs.TokenProgram
import com.solana.vendor.ResultError

interface ISolanaWalletManager {

    fun getSupportedTokens():List<Token>

    fun getSolanaRpc(): Solana

    fun closeTokenAccount(
        tokenPubkey: PublicKey,
        onComplete: ((Result<Pair<String, PublicKey>>) -> Unit)
    )

    fun checkSPLTokenAccountExistence(
        mintAddress: PublicKey,
        destinationAddress: PublicKey,
        onComplete: ((com.solana.vendor.Result<SPLTokenDestinationAddress, ResultError>) -> Unit)
    )

    fun getTokenWallets(
        onComplete: ((Result<List<Wallet>>) -> Unit)
    )

    fun createTokenAccount(
        mintAddress: PublicKey,
        onComplete: ((Result<Pair<String, PublicKey>>) -> Unit)
    )

    fun getMintData(
        mintAddress: PublicKey,
        programId: PublicKey = TokenProgram.PROGRAM_ID,
        onComplete: ((Result<Mint>) -> Unit)
    )

    fun findSPLTokenDestinationAddress(
        mintAddress: PublicKey,
        destinationAddress: PublicKey,
        allowUnfundedRecipient: Boolean = false,
        onComplete: ((com.solana.vendor.Result<SPLTokenDestinationAddress, ResultError>) -> Unit)
    )

    fun sendSOL(
        destination: PublicKey,
        amount: Long,
        onComplete: ((Result<String>) -> Unit)
    )

    fun sendSPLTokens(
        mintAddress: PublicKey,
        fromPublicKey: PublicKey,
        destinationAddress: PublicKey,
        amount: Long,
        allowUnfundedRecipient: Boolean = false,
        onComplete: ((Result<String>) -> Unit)
    )

    fun serializeAndSendWithFee(transaction: Transaction,
                                recentBlockHash: String? = null,
                                onComplete: ((Result<String>) -> Unit)
    )
}