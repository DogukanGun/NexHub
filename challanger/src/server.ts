// Import the framework and instantiate it
import Fastify from 'fastify'
import 'dotenv/config'
import { connect } from './utils/mongo/connect'
import router from './routers'
import fastifySwagger from '@fastify/swagger'
import fastifySwaggerUi from '@fastify/swagger-ui'

const fastify = Fastify({
  logger: true
})

try {
    // Register Swagger
    fastify.register(fastifySwagger, {
        swagger: {
            info: {
                title: 'NexHub API',
                description: 'API documentation for NexHub platform',
                version: '1.0.0'
            },
            host: 'localhost:3000',
            schemes: ['http'],
            consumes: ['application/json'],
            produces: ['application/json']
        }
    })

    // Register Swagger UI
    fastify.register(fastifySwaggerUi, {
        routePrefix: '/docs',
        uiConfig: {
            docExpansion: 'full',
            deepLinking: false
        }
    })

    connect().then(() => {
        router(fastify)
    })
} catch (err) {
  fastify.log.error(err)
}