import { AIMessage } from "../../models/aiMessage";

const askToAgentForNewUserPrompt = (launchpadName: string, launchpadDescription: string) => `
You are a helpful assistant that understand if the user really knows the project. I am giving you the information about the project.

Project Name: ${launchpadName}
Project Description: ${launchpadDescription}

So now ask some challenging questions to the user to understand if they really know the project.
The response should be in the following format:
{
    "decision": "ask_more_questions",
    "prompt": "string"
}
`;

const askToAgentForOngoingChatPrompt = (launchpadName: string, launchpadDescription: string, messageHistory: AIMessage[]) => `
You are a helpful assistant that helps users to continue a chat.

Project Name: ${launchpadName}
Project Description: ${launchpadDescription}


Here is the message history:
${messageHistory.map((message) => `${message.role}: ${message.content}`).join("\n")}

So now if you need to ask more question to understand the user knowledge about the project, ask them. Otherwise if you are gonna decide make the decision.
If the decision is made, the response should be in the following format:
{
    "decision": "yes" | "no",
    "promt": "string"
}
Otherwise, if you need to ask more question, the response should be in the following format:
{
    "decision": "ask_more_questions",
    "prompt": "string"
}


`;

export { askToAgentForNewUserPrompt, askToAgentForOngoingChatPrompt };