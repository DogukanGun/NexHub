import mongoose from 'mongoose';

export const connect = async () => {
    if (!process.env.MONGO_URI) {
        throw new Error("MONGO_URI is not set");
    }
    const uri = process.env.MONGO_URI;
    await mongoose.connect(uri);
    console.log("Successfully connected to MongoDB!");
}