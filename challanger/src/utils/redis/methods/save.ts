import client from "../connect";


const saveToRedis = async (key: string, field: string, value: any) => {
    await client.hSet(key, field, value);
}

export default saveToRedis;