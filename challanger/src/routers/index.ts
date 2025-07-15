import { FastifyInstance } from "fastify";

const router = async (fastify: FastifyInstance) => {
    fastify.get('/', async (request, reply) => {
        return { hello: 'world' }
    })
    fastify.listen({ port: 3000 })
}

export default router;