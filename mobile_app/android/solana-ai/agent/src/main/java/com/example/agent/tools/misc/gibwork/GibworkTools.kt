package com.example.agent.tools.misc.gibwork

import androidx.fragment.app.FragmentActivity
import com.dag.wallet.SolanaWalletManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GibworkTools(private val walletManager: SolanaWalletManager) {
    private val client = HttpClient(CIO) {}

    @Serializable
    private data class TokenInfo(
        @SerialName("mintAddress")
        val mintAddress: String,
        val amount: Long
    )

    @Serializable
    private data class CreateTaskRequest(
        val title: String,
        val content: String,
        val requirements: String,
        val tags: List<String>,
        val payer: String,
        val token: TokenInfo
    )

    @Serializable
    private data class CreateTaskResponse(
        val taskId: String? = null,
        @SerialName("serializedTransaction")
        val serializedTransaction: String? = null,
        val message: String? = null
    )

    @Serializable
    private data class TaskCreationResult(
        val status: String,
        val taskId: String,
        val signature: String
    )

    fun createTask(
        title: String,
        content: String,
        requirements: String,
        tags: List<String>,
        tokenMintAddress: String,
        tokenAmount: Long,
        payer: String? = null,
        activity: FragmentActivity
    ): String {
        val deferred = CompletableDeferred<String>()

        runBlocking {
            try {
                // Create the request body
                val requestBody = CreateTaskRequest(
                    title = title,
                    content = content,
                    requirements = requirements,
                    tags = tags,
                    payer = payer ?: walletManager.getPublicKey().publicKey.toBase58(),
                    token = TokenInfo(
                        mintAddress = tokenMintAddress,
                        amount = tokenAmount
                    )
                )

                // Make API call to get transaction
                val response = client.post("https://api2.gib.work/tasks/public/transaction") {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

                val taskResponse = response.body<CreateTaskResponse>()

                if (taskResponse.taskId == null || taskResponse.serializedTransaction == null) {
                    throw Exception(taskResponse.message ?: "Failed to create task")
                }

                // Sign and send transaction
                walletManager.signTransaction(
                    txAsBase58 = taskResponse.serializedTransaction,
                    activity = activity,
                    onResult = { signedTx ->
                        val result = TaskCreationResult(
                            status = "success",
                            taskId = taskResponse.taskId,
                            signature = signedTx.toString()
                        )
                        deferred.complete("Task created successfully: $result")
                    },
                    onFailure = {
                        deferred.complete("Failed to sign transaction")
                    }
                )

            } catch (e: Exception) {
                deferred.complete("Failed to create task: ${e.message}")
            }
        }

        return runBlocking { deferred.await() }
    }

    suspend fun getTaskDetails(taskId: String): String {
        return try {
            val response = client.get("https://api2.gib.work/tasks/public/$taskId")
            response.body<String>()
        } catch (e: Exception) {
            "Failed to get task details: ${e.message}"
        }
    }

    suspend fun listTasks(): String {
        return try {
            val response = client.get("https://api2.gib.work/tasks/public")
            response.body<String>()
        } catch (e: Exception) {
            "Failed to list tasks: ${e.message}"
        }
    }
}