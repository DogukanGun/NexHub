import { FastifyReply } from "fastify";
import { AskAgentRequest } from "../../models/request/askAgent.request";
import { getFromRedis } from "../../utils/redis/methods/get";
import saveToRedis from "../../utils/redis/methods/save";
import { AIMessage } from "../../models/aiMessage";
import askToAgentService from "../../services/agent/askToAgent.service";

/**
 * Handles the agent interaction for a user's message
 * 
 * This controller manages the conversation flow with an AI agent:
 * - Retrieves existing message history from Redis
 * - Creates a new message history if none exists
 * - Prepares the message for AI processing
 * - Saves the updated message history back to Redis
 * 
 * @param {AskAgentRequest} req - The incoming request containing user message and user ID
 * @param {FastifyReply} res - The Fastify reply object for sending response
 * @returns {Promise<string>} The AI's response
 */
const askToAgent = async (req: AskAgentRequest, res: FastifyReply) => {
    const { message, userId } = req;
    const messageHistory = await getFromRedis("message_history", userId);
    let messageHistoryToSend: AIMessage[] = [];
    if (!messageHistory) {
        // Create a new message history
        const initialMessageHistory = JSON.stringify([{
            role: "user",
            content: message,
        } as AIMessage]);
        
        // Save the message history to redis
        await saveToRedis("message_history", userId, initialMessageHistory);
        messageHistoryToSend = JSON.parse(initialMessageHistory);
    } else {
        const messageHistoryAsJson = JSON.parse(messageHistory);
        messageHistoryAsJson.push({
            role: "user",
            content: message,
        } as AIMessage);
        await saveToRedis("message_history", userId, JSON.stringify(messageHistoryAsJson));
        messageHistoryToSend = messageHistoryAsJson;
    }
    const response = await askToAgentService(message, messageHistoryToSend);
    return response;
}

export default askToAgent;  