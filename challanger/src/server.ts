// Import the framework and instantiate it
import Fastify from 'fastify'
import 'dotenv/config'
import { connect } from '../utils/mongo/connect'
import router from './routers'

const fastify = Fastify({
  logger: true
})

try {
    connect().then(() => {
        router(fastify)
    })
} catch (err) {
  fastify.log.error(err)
}