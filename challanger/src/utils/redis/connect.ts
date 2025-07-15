import { createClient } from "redis";


const client = await createClient({
    username: process.env.REDIS_USER,
    password: process.env.REDIS_PASSWORD,
    socket: {
        host: process.env.REDIS_URL,
        port: 18619
    }
})
  .on("error", (err) => console.log("Redis Client Error", err))
  .connect();

export default client;