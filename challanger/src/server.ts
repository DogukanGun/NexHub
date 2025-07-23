// Import the framework and instantiate it
import Fastify from 'fastify'
import 'dotenv/config'
import fastifySwagger from '@fastify/swagger'
import fastifySwaggerUi from '@fastify/swagger-ui'
import { connect } from './utils/mongo/connect.js'
import router from './routers/index.js'

const fastify = Fastify({
  logger: true
})

const start = async () => {
  try {
    // Register Swagger first
    await fastify.register(fastifySwagger, {
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
    await fastify.register(fastifySwaggerUi, {
      routePrefix: '/docs',
      uiConfig: {
        docExpansion: 'full',
        deepLinking: false
      }
    })

    // Register routes
    await fastify.register(router)
    
    // Connect to MongoDB
    await connect()
    
    // Make sure fastify is ready before starting
    await fastify.ready()
    
    // Start the server
    await fastify.listen({ 
      port: 3000,
      host: '0.0.0.0' // This allows connections from all interfaces
    })
    
    console.log('Server is running on http://localhost:3000')
    console.log('Documentation available at http://localhost:3000/docs')
  } catch (err) {
    fastify.log.error(err)
    process.exit(1)
  }
}

start()