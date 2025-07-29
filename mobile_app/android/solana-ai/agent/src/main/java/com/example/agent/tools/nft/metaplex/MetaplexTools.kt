package com.example.agent.tools.nft.metaplex

import com.dag.wallet.SolanaWalletManager
import com.metaplex.lib.Metaplex
import com.metaplex.lib.drivers.indenty.ReadOnlyIdentityDriver
import com.metaplex.lib.drivers.storage.OkHttpSharedStorageDriver
import com.metaplex.lib.experimental.jen.tokenmetadata.Creator
import com.metaplex.lib.modules.candymachines.models.CandyMachine
import com.metaplex.lib.modules.candymachinesv2.models.CandyMachineV2
import com.metaplex.lib.modules.nfts.TokenMetadataAuthority
import com.metaplex.lib.modules.nfts.TokenMetadataAuthorizationDetails
import com.metaplex.lib.modules.nfts.builders.TransferNftInput
import com.metaplex.lib.modules.nfts.models.Metadata
import com.metaplex.lib.solana.SolanaConnectionDriver
import com.solana.core.PublicKey
import com.solana.networking.RPCEndpoint

class MetaplexTools(private val walletManager: SolanaWalletManager) {

    private fun prepareMetaplex(): Metaplex {
        val keypair = walletManager.getPublicKey()
        val pk = keypair.publicKey
        val solanaConnection = SolanaConnectionDriver(RPCEndpoint.mainnetBetaSolana)
        val solanaRpc = walletManager.getSolanaRpc()
        val solanaIdentityDriver = ReadOnlyIdentityDriver(pk, solanaRpc.api)
        val storageDriver = OkHttpSharedStorageDriver()
        return Metaplex(solanaConnection, solanaIdentityDriver, storageDriver)
    }

    suspend fun createNft(
        name: String,
        symbol: String = "",
        uri: String,
        sellerFeeBasisPoints: Int,
        creators: List<Creator>? = null,
        collection: PublicKey? = null,
        isMutable: Boolean = true,
        isCollection: Boolean = false
    ): String {
        val metaplex = prepareMetaplex()
        return try {
            val nft = metaplex.nft.create(
                Metadata(
                    name,
                    symbol,
                    uri,
                    sellerFeeBasisPoints,
                    creators,
                    collection,
                    isMutable
                ),
                isCollection
            ).getOrThrow()
            "NFT created successfully: ${nft.mint.toBase58()}"
        } catch (e: Exception) {
            "Failed to create NFT: ${e.message}"
        }
    }

    suspend fun createCandyMachine(
        itemsAvailable: Long,
        sellerFeeBasisPoints: Int,
        collection: PublicKey,
        collectionUpdateAuthority: PublicKey,
    ): String {
        val metaplex = prepareMetaplex()
        return try {
            val candyMachine = metaplex.candyMachines.create(
                itemsAvailable = itemsAvailable,
                sellerFeeBasisPoints = sellerFeeBasisPoints,
                collection = collection,
                collectionUpdateAuthority = collectionUpdateAuthority,
            ).getOrThrow()
            "Candy Machine created successfully: ${candyMachine.address.toBase58()}"
        } catch (e: Exception) {
            "Failed to create Candy Machine: ${e.message}"
        }
    }

    suspend fun mintNftFromCandyMachine(candyMachine: CandyMachine): String {
        val metaplex = prepareMetaplex()
        return try {
            val nft = metaplex.candyMachines.mintNft(candyMachine).getOrThrow()
            "NFT minted successfully from Candy Machine: ${nft.mint.toBase58()}"
        } catch (e: Exception) {
            "Failed to mint NFT from Candy Machine: ${e.message}"
        }
    }

    suspend fun findCandyMachineByAddress(address: String): String {
        val metaplex = prepareMetaplex()
        return try {
            val candyMachine = metaplex.candyMachines.findByAddress(PublicKey(address)).getOrThrow()
            "Found Candy Machine: ${candyMachine.address.toBase58()}"
        } catch (e: Exception) {
            "Failed to find Candy Machine: ${e.message}"
        }
    }

    suspend fun setCandyMachineCollection(candyMachine: CandyMachine, collectionAddress: String): String {
        val metaplex = prepareMetaplex()
        return try {
            val result = metaplex.candyMachines.setCollection(
                candyMachine,
                PublicKey(collectionAddress)
            ).getOrThrow()
            "Collection set successfully for Candy Machine"
        } catch (e: Exception) {
            "Failed to set collection: ${e.message}"
        }
    }

    suspend fun createCandyMachineV2(
        price: Long,
        sellerFeeBasisPoints: Int,
        itemsAvailable: Long,
    ): String {
        val metaplex = prepareMetaplex()
        return try {
            val candyMachine = metaplex.candyMachinesV2.create(
                price,sellerFeeBasisPoints,itemsAvailable
            ).getOrThrow()
            "Candy Machine V2 created successfully: ${candyMachine.address.toBase58()}"
        } catch (e: Exception) {
            "Failed to create Candy Machine V2: ${e.message}"
        }
    }

    suspend fun mintNftFromCandyMachineV2(candyMachine: CandyMachineV2): String {
        val metaplex = prepareMetaplex()
        return try {
            val nft = metaplex.candyMachinesV2.mintNft(candyMachine).getOrThrow()
            "NFT minted successfully from Candy Machine V2: ${nft.mint.toBase58()}"
        } catch (e: Exception) {
            "Failed to mint NFT from Candy Machine V2: ${e.message}"
        }
    }

    suspend fun findCandyMachineV2ByAddress(address: String): String {
        val metaplex = prepareMetaplex()
        return try {
            val candyMachine = metaplex.candyMachinesV2.findByAddress(PublicKey(address)).getOrThrow()
            "Found Candy Machine V2: ${candyMachine.address.toBase58()}"
        } catch (e: Exception) {
            "Failed to find Candy Machine V2: ${e.message}"
        }
    }

    suspend fun transferNft(
        mintKey: PublicKey, // also know as Token Address
        authority: TokenMetadataAuthority? = null,
        authorizationDetails: TokenMetadataAuthorizationDetails? = null,
        fromOwner : PublicKey? = null,
        fromToken : PublicKey? = null,
        toOwner : PublicKey,
        toToken : PublicKey? = null,
        amount : ULong = 1u,
    ): String {
        val metaplex = prepareMetaplex()
        return try {
            val result = metaplex.nft.transfer(
                TransferNftInput(
                    mintKey,
                    authority,
                    authorizationDetails,
                    fromOwner,
                    fromToken,
                    toOwner,
                    toToken,
                    amount
                )
            ).getOrThrow()
            "NFT transferred successfully to: ${toOwner.toBase58()}"
        } catch (e: Exception) {
            "Failed to transfer NFT: ${e.message}"
        }
    }

    suspend fun findNftsByOwner(ownerAddress: String): String {
        val metaplex = prepareMetaplex()
        return try {
            val nfts = metaplex.nft.findAllByOwner(PublicKey(ownerAddress)).getOrThrow()
            "Found ${nfts.size} NFTs for owner: $ownerAddress\n" +
                    nfts.joinToString("\n") { "- ${it?.mint?.toBase58()}: ${it?.metadataAccount.toString()}" }
        } catch (e: Exception) {
            "Failed to find NFTs by owner: ${e.message}"
        }
    }

    suspend fun findNftsByCandyMachine(candyMachine: CandyMachine, version: Int = 1): String {
        val metaplex = prepareMetaplex()
        return try {
            val nfts = metaplex.nft.findAllByCandyMachine(candyMachine.address, version).getOrThrow()
            "Found ${nfts.size} NFTs from Candy Machine: ${candyMachine.address.toBase58()}\n" +
                    nfts.joinToString("\n") { "- ${it?.mint?.toBase58()}: ${it?.metadataAccount.toString()}" }
        } catch (e: Exception) {
            "Failed to find NFTs by Candy Machine: ${e.message}"
        }
    }

    suspend fun findNftsByCreator(creatorAddress: String): String {
        val metaplex = prepareMetaplex()
        return try {
            val nfts = metaplex.nft.findAllByCreator(PublicKey(creatorAddress)).getOrThrow()
            "Found ${nfts.size} NFTs by creator: $creatorAddress\n" +
                    nfts.joinToString("\n") { "- ${it?.mint?.toBase58()}: ${it?.metadataAccount.toString()}" }
        } catch (e: Exception) {
            "Failed to find NFTs by creator: ${e.message}"
        }
    }

    suspend fun findNftsByMintList(mintAddresses: List<String>): String {
        val metaplex = prepareMetaplex()
        return try {
            val publicKeys = mintAddresses.map { PublicKey(it) }
            val nfts = metaplex.nft.findAllByMintList(publicKeys).getOrThrow()
            "Found ${nfts.size} NFTs:\n" +
                    nfts.joinToString("\n") { "- ${it?.mint?.toBase58()}: ${it?.metadataAccount.toString()}" }
        } catch (e: Exception) {
            "Failed to find NFTs by mint list: ${e.message}"
        }
    }
}