import mongoose, { Schema, Document } from 'mongoose';

/**
 * Interface representing a Launchpad document
 */
export interface ILaunchpad extends Document {
  name: string;
  description: string;
  address: string;
  price: number;
  projectSocialLink: string[];
  createdBy: string;
  createdAt: Date;
  updatedAt: Date;
}

/**
 * Mongoose schema for the Launchpad model
 */
const LaunchpadSchema: Schema = new Schema(
  {
    name: {
      type: String,
      required: true,
      trim: true
    },
    description: {
      type: String,
      required: true
    },
    address: {
      type: String,
      required: true,
      unique: true
    },
    price: {
      type: Number,
      required: true
    },
    projectSocialLink: {
      type: [String],
      default: []
    },
    createdBy: {
      type: String,
      required: true
    }
  },
  {
    timestamps: true
  }
);

// Create and export the Launchpad model
const LaunchpadModel = mongoose.model<ILaunchpad>('Launchpad', LaunchpadSchema);

export default LaunchpadModel;