import { FastifyReply } from "fastify";
import LaunchpadModel from "../../models/db/Launchpad.model";

const getLaunchpads = async (res: FastifyReply) => {
    const launchpads = await LaunchpadModel.find();
    res.status(200).send(launchpads);
}

export default getLaunchpads;   