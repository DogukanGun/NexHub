import { FastifyReply } from "fastify";
import LaunchpadModel from "../../models/db/Launchpad.model";

/**
 * Retrieves all launchpads from the database
 * 
 * This controller fetches all launchpad records:
 * - Queries the LaunchpadModel to get all launchpad entries
 * - Returns the retrieved launchpads
 * 
 * @param {FastifyReply} res - The Fastify reply object for sending response
 * @returns {Promise<object[]>} List of launchpads
 */
const getLaunchpads = async (res: FastifyReply) => {
    const launchpads = await LaunchpadModel.find();
    return launchpads;
}

export default getLaunchpads;   