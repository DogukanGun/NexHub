import client from "../connect";

/**
 * Saves a value to a specific field in a Redis hash
 * 
 * @param {string} key - The Redis hash key
 * @param {string} field - The specific field within the hash to save
 * @param {any} value - The value to be saved in the specified field
 * @returns {Promise<void>}
 */
const saveToRedis = async (key: string, field: string, value: any) => {
    await client.hSet(key, field, value);
}

export default saveToRedis;