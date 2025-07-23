import { OpenAI } from "openai";
import { AIMessage } from "../../models/aiMessage";
import { askToAgentForNewUserPrompt, askToAgentForOngoingChatPrompt } from "./promt";
import LaunchpadModel from "../../models/db/Launchpad.model";

/**
 * Sends a message to the AI agent and generates a response
 * 
 * @param {string} message - The user's input message
 * @param {AIMessage[]} messageHistory - The existing conversation history
 * @param {string} launchpadId - The ID of the launchpad
 * @returns {Promise<string>} The AI's generated response
 */
const askToAgent = async (message: string, messageHistory: AIMessage[], launchpadId: string) => {
    const openai = new OpenAI({
        apiKey: process.env.OPENAI_API_KEY,
    });
    const launchpad = await LaunchpadModel.findById(launchpadId);
    if (!launchpad) {
        throw new Error("Launchpad not found");
    }
    const prompt = messageHistory.length === 0 ? askToAgentForNewUserPrompt(launchpad.name, launchpad.description) : askToAgentForOngoingChatPrompt(launchpad.name, launchpad.description, messageHistory);
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
        model: "gpt-4.1-mini",
        messages: messageHistory,
    });

    return response.choices[0].message.content;
}

export default askToAgent;