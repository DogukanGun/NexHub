import mongoose from "mongoose";

const Schema = mongoose.Schema;
const ObjectId = Schema.ObjectId;

const LaunchpadSchema = new Schema({
  name: { type: String, required: true },
  description: String,
  address: String,
  price: { type: Number, required: true },
  projectSocialLink: { type: [String], required: true },
  createdBy: { type: String, required: true },
  isActive: { type: Boolean, default: true },
}, {
  timestamps: true
});

const LaunchpadModel = mongoose.model('Launchpad', LaunchpadSchema);

export default LaunchpadModel;