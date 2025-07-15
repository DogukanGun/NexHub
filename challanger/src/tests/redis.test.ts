import { createClient } from 'redis';
import dotenv from 'dotenv';

// Load environment variables
dotenv.config();

describe('Redis Connection', () => {
    let client: ReturnType<typeof createClient>;

    beforeAll(async () => {
        // Ensure environment variables are set
        expect(process.env.REDIS_URL).toBeDefined();
        expect(process.env.REDIS_USER).toBeDefined();
        expect(process.env.REDIS_PASSWORD).toBeDefined();
    });

    it('should create a Redis client', async () => {
        client = createClient({
            username: process.env.REDIS_USER,
            password: process.env.REDIS_PASSWORD,
            socket: {
                host: process.env.REDIS_URL,
                port: 18619
            }
        });

        expect(client).toBeDefined();
    });

    it('should connect to Redis', async () => {
        await expect(client.connect()).resolves.not.toThrow();
    });

    it('should set and get a value', async () => {
        const testKey = 'test:key';
        const testValue = 'test-value';

        await client.set(testKey, testValue);
        const retrievedValue = await client.get(testKey);

        expect(retrievedValue).toBe(testValue);
    });

    it('should handle connection errors', async () => {
        const errorClient = createClient({
            username: 'invalid-user',
            password: 'invalid-password',
            socket: {
                host: 'invalid-host',
                port: 18619
            }
        });

        await expect(errorClient.connect()).rejects.toThrow();
    });

    afterAll(async () => {
        if (client) {
            await client.quit();
        }
    });
});
