package com.dag.aiagent

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.model.output.Response
import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.model.StreamingResponseHandler
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel

sealed class AiModel {
    data class OpenAI(val apiKey: String) : AiModel()
    data class Gemini(val apiKey: String) : AiModel()
}

open class AiAgent(protected val model: AiModel) {

    private val chatModel: ChatLanguageModel = when (model) {
        is AiModel.OpenAI -> OpenAiChatModel.builder()
            .apiKey(model.apiKey)
            .build()
        is AiModel.Gemini -> GoogleAiGeminiChatModel.builder()
            .apiKey(model.apiKey)
            .modelName("gemini-1.5-pro")
            .build()
    }

    private val streamingModel: StreamingChatLanguageModel = when (model) {
        is AiModel.OpenAI -> OpenAiStreamingChatModel.builder()
            .apiKey(model.apiKey)
            .build()
        is AiModel.Gemini -> GoogleAiGeminiStreamingChatModel.builder()
            .apiKey(model.apiKey)
            .modelName("gemini-1.5-pro")
            .build()
    }

    fun generate(
        messages: List<ChatMessage>,
        toolSpecifications: MutableList<ToolSpecification>,
        onNext: (String) -> Unit = {},
        onComplete: (Response<AiMessage>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            streamingModel.generate(messages, toolSpecifications, object : StreamingResponseHandler<AiMessage> {
                override fun onNext(token: String) {
                    onNext(token)
                }

                override fun onComplete(response: Response<AiMessage>) {
                    onComplete(response)
                }

                override fun onError(error: Throwable) {
                    onError(error)
                }
            })
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun generateNonStreaming(
        messages: List<ChatMessage>,
        toolSpecifications: MutableList<ToolSpecification>,
        onComplete: (Response<AiMessage>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            val response = chatModel.generate(messages, toolSpecifications)
            onComplete(response)
        } catch (e: Exception) {
            onError(e)
        }
    }

    companion object {
        const val DEFAULT_TEMPERATURE = 0.7
        const val DEFAULT_MAX_TOKENS = 2000
    }
}