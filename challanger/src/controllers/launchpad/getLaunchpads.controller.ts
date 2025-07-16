import { FastifyReply } from "fastify";
import LaunchpadModel from "../../models/db/Launchpad.model";

/**
 * Retrieves all launchpads from the database
 * 
 * This controller fetches all launchpad records:
 * - Queries the LaunchpadModel to get all launchpad entries
 * - Sends the retrieved launchpads as a response
 * 
 * @param {FastifyReply} res - The Fastify reply object for sending response
 * @returns {Promise<void>}
 */
const getLaunchpads = async (res: FastifyReply) => {
    const launchpads = await LaunchpadModel.find();
    res.status(200).send(launchpads);
}

export default getLaunchpads;   