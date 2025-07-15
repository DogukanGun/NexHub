
import { ethers } from "ethers";
import LaunchpadModel from "../../models/db/Launchpad.model";
import CreateLaunchpadRequest from "../../models/request/createLaunchpad.request";
import { FastifyReply } from "fastify";
import LaunchpadFactoryJSON from "../../abi/LaunchpadFactory.json";

const createLaunchpad = async (req: CreateLaunchpadRequest, res: FastifyReply) => {    
    const { name, description, price, createdBy, projectSocialLink } = req;

    if (!process.env.RPC_URL || !process.env.PRIVATE_KEY || !process.env.USDC_TOKEN_ADDRESS) {
        return res.status(500).send({
            message: "Environment variables are not configured",
        });
    }
    
    const provider = new ethers.JsonRpcProvider(process.env.RPC_URL);
    const signer = new ethers.Wallet(process.env.PRIVATE_KEY!, provider);

    const usdcTokenAddress = process.env.USDC_TOKEN_ADDRESS;
    if (!usdcTokenAddress) {
        return res.status(500).send({
            message: "USDC token address is not configured",
        });
    }

    try {
        // Create contract factory with ABI and signer
        const LaunchpadFactoryFactory = new ethers.ContractFactory(
            LaunchpadFactoryJSON.abi, 
            LaunchpadFactoryJSON.bytecode, 
            signer
        );

        // Deploy LaunchpadFactory contract
        const launchpadFactory = await LaunchpadFactoryFactory.deploy(
            createdBy, 
            usdcTokenAddress
        );

        // Wait for contract deployment
        await launchpadFactory.waitForDeployment();
        const launchpadFactoryAddress = await launchpadFactory.getAddress();

        const dbRes = await LaunchpadModel.create({
            name: name,
            description: description,
            address: launchpadFactoryAddress,
            price,
            projectSocialLink: projectSocialLink,
            createdBy: createdBy,
        });
        const savedLaunchpad = await dbRes.save();
    
        res.status(201).send({
          message: "Launchpad created successfully",
          data: savedLaunchpad,
        });
    } catch (error) {
        console.error("Launchpad creation error:", error);
        res.status(500).send({
            message: "Failed to create launchpad",
            error: error instanceof Error ? error.message : error,
        });
    }
}

export default createLaunchpad;