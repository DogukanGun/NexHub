export type AIMessage = {
    role: "user" | "assistant" | "system";
    content: string;
}