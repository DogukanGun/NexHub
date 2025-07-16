import { OpenAI } from "openai";
import { AIMessage } from "../../models/aiMessage";
import { askToAgentForNewUserPrompt, askToAgentForOngoingChatPrompt } from "./promt";

/**
 * Sends a message to the AI agent and generates a response
 * 
 * @param {string} message - The user's input message
 * @param {AIMessage[]} messageHistory - The existing conversation history
 * @returns {Promise<string>} The AI's generated response
 */
const askToAgent = async (message: string,messageHistory: AIMessage[]) => {
    const openai = new OpenAI({
        apiKey: process.env.OPENAI_API_KEY,
    });
    const prompt = messageHistory.length === 0 ? askToAgentForNewUserPrompt : askToAgentForOngoingChatPrompt;
    if (messageHistory.length === 0) {
        messageHistory.push({
            role: "user",
            content: message,
        });
    }
    messageHistory.push({
        role: "system",
        content: prompt,
    });
    const response = await openai.chat.completions.create({
        model: "gpt-4o-mini",
        messages: messageHistory,
    });

    return response.choices[0].message.content;
}

export default askToAgent;