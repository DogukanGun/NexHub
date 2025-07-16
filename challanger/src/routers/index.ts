import { FastifyInstance, FastifyRequest, FastifyReply } from "fastify";
import askToAgent from "../controllers/agent/agent.controller";
import createLaunchpad from "../controllers/launchpad/createLaunchpad.controller";
import getLaunchpads from "../controllers/launchpad/getLaunchpads.controller";
import { AskAgentRequest } from "../models/request/askAgent.request";
import CreateLaunchpadRequest from "../models/request/createLaunchpad.request";

const router = async (fastify: FastifyInstance) => {
    // Root route
    fastify.get('/', {
        schema: {
            description: 'Health check endpoint',
            tags: ['Health'],
            response: {
                200: {
                    description: 'Successful response',
                    type: 'object',
                    properties: {
                        hello: { type: 'string' }
                    }
                }
            }
        }
    }, async (request: FastifyRequest, reply: FastifyReply) => {
        return { hello: 'world' }
    })

    // Agent route
    fastify.post('/agent/ask', {
        schema: {
            description: 'Ask AI agent a question',
            tags: ['Agent'],
            body: {
                type: 'object',
                properties: {
                    message: { type: 'string' },
                    userId: { type: 'string' }
                },
                required: ['message', 'userId']
            },
            response: {
                200: {
                    description: 'Successful AI response',
                    type: 'string'
                }
            }
        }
    }, async (request: FastifyRequest<{ Body: AskAgentRequest }>, reply: FastifyReply) => {
        const { message, userId } = request.body;
        return await askToAgent({ message, userId }, reply);
    })

    // Launchpad routes
    fastify.post('/launchpad/create', {
        schema: {
            description: 'Create a new launchpad',
            tags: ['Launchpad'],
            body: {
                type: 'object',
                properties: {
                    name: { type: 'string' },
                    description: { type: 'string' },
                    price: { type: 'number' },
                    createdBy: { type: 'string' },
                    projectSocialLink: { 
                        type: 'array', 
                        items: { type: 'string' } 
                    }
                },
                required: ['name', 'description', 'price', 'createdBy', 'projectSocialLink']
            },
            response: {
                201: {
                    description: 'Launchpad created successfully',
                    type: 'object',
                    properties: {
                        message: { type: 'string' },
                        data: { 
                            type: 'object',
                            properties: {
                                name: { type: 'string' },
                                description: { type: 'string' },
                                address: { type: 'string' },
                                price: { type: 'number' }
                            }
                        }
                    }
                }
            }
        }
    }, async (request: FastifyRequest<{ Body: CreateLaunchpadRequest }>, reply: FastifyReply) => {
        const { name, description, price, createdBy, projectSocialLink } = request.body;
        return await createLaunchpad({ name, description, price, createdBy, projectSocialLink }, reply);
    })

    fastify.get('/launchpads', {
        schema: {
            description: 'Get all launchpads',
            tags: ['Launchpad'],
            response: {
                200: {
                    description: 'List of launchpads',
                    type: 'array',
                    items: {
                        type: 'object',
                        properties: {
                            name: { type: 'string' },
                            description: { type: 'string' },
                            address: { type: 'string' },
                            price: { type: 'number' }
                        }
                    }
                }
            }
        }
    }, async (request: FastifyRequest, reply: FastifyReply) => {
        return await getLaunchpads(reply);
    })

    // Listen on port 3000
    fastify.listen({ port: 3000 })
}

export default router;