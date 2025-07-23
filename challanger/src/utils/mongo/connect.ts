import mongoose from 'mongoose';

/**
 * Establishes a connection to MongoDB using the connection string from environment variables
 * 
 * @returns {Promise<typeof mongoose>} A promise that resolves to the mongoose instance
 * @throws {Error} If the connection fails
 */
export const connect = async (): Promise<typeof mongoose> => {
  try {
    const mongoURI = process.env.MONGODB_URI || process.env.MONGO_URI || 'mongodb://localhost:27017/nexhub';
    console.log(mongoURI);
    console.log('Connecting to MongoDB...');
    const connection = await mongoose.connect(mongoURI);
    console.log('MongoDB connected successfully');
    return connection;
  } catch (error) {
    console.error('MongoDB connection error:', error);
    throw error;
  }
};

/**
 * Closes the MongoDB connection
 * 
 * @returns {Promise<void>}
 */
export const disconnect = async (): Promise<void> => {
  try {
    await mongoose.disconnect();
    console.log('MongoDB disconnected');
  } catch (error) {
    console.error('MongoDB disconnect error:', error);
    throw error;
  }
};