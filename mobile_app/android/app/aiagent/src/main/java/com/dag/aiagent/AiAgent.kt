package com.dag.aiagent
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.model.output.Response
import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.model.StreamingResponseHandler
import dev.langchain4j.model.openai.OpenAiChatModelName
import dev.langchain4j.model.openai.OpenAiStreamingChatModel

abstract class AiAgent {

    fun generate(
        messages: List<ChatMessage>,
        apiKey: String,
        toolSpecifications: MutableList<ToolSpecification>,
        onComplete:(response: Response<AiMessage>) -> Unit
    ) {
        val chatModel = OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder()
            .apiKey(apiKey)
            .modelName(OpenAiChatModelName.GPT_4)
            .build()
        chatModel.generate(messages,toolSpecifications,object : StreamingResponseHandler<AiMessage> {
            override fun onNext(token: String) {
                //TODO
            }

            override fun onComplete(response: Response<AiMessage>) {
                onComplete(response)
            }

            override fun onError(error: Throwable) {
                error.printStackTrace()
            }
        })
    }

}