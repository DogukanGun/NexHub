import client from "../connect";

const getFromRedis = async (key: string, field: string) => {
    return await client.hGet(key, field);
}

const getFromRedisAll = async (key: string) => {    
    return await client.hGetAll(key);
}

export { getFromRedis, getFromRedisAll };