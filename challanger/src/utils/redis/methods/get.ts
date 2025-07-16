import client from "../connect";

/**
 * Retrieves a specific field from a Redis hash
 * 
 * @param {string} key - The Redis hash key
 * @param {string} field - The specific field within the hash to retrieve
 * @returns {Promise<string | null>} The value of the field or null if not found
 */
const getFromRedis = async (key: string, field: string) => {
    return await client.hGet(key, field);
}

/**
 * Retrieves all fields and their values from a Redis hash
 * 
 * @param {string} key - The Redis hash key
 * @returns {Promise<Record<string, string>>} An object containing all fields and values in the hash
 */
const getFromRedisAll = async (key: string) => {    
    return await client.hGetAll(key);
}

export { getFromRedis, getFromRedisAll };