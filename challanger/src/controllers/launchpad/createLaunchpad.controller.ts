
import { ethers } from "ethers";
import LaunchpadModel from "../../models/db/Launchpad.model";
import CreateLaunchpadRequest from "../../models/request/createLaunchpad.request";
import { FastifyReply } from "fastify";
import LaunchpadFactoryJSON from "../../abi/LaunchpadFactory.json";

/**
 * Creates a new launchpad with the provided details
 * 
 * This controller handles the process of creating a launchpad:
 * - Validates environment configuration
 * - Selects appropriate RPC URL based on environment
 * - Deploys a LaunchpadFactory contract
 * - Saves launchpad details to the database
 * 
 * @param {CreateLaunchpadRequest} req - The request containing launchpad creation details
 * @param {FastifyReply} res - The Fastify reply object for sending response
 * @returns {Promise<object>} The created launchpad details
 */
const createLaunchpad = async (req: CreateLaunchpadRequest, res: FastifyReply) => {    
    const { name, description, price, createdBy, projectSocialLink } = req;

    if (!process.env.PRIVATE_KEY || !process.env.USDC_TOKEN_ADDRESS) {
        return {
            message: "Environment variables are not configured",
        };
    }
    let rpcUrl = process.env.BNB_TESTNET_RPC;
    if (process.env.ENV === "production") {
        rpcUrl = process.env.BNB_MAINNET_RPC;
    }
    const provider = new ethers.JsonRpcProvider(rpcUrl);
    const signer = new ethers.Wallet(process.env.PRIVATE_KEY!, provider);

    const usdcTokenAddress = process.env.USDC_TOKEN_ADDRESS;
    if (!usdcTokenAddress) {
        return {
            message: "USDC token address is not configured",
        };
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
    
        return {
          message: "Launchpad created successfully",
          data: savedLaunchpad,
        };
    } catch (error) {
        console.error("Launchpad creation error:", error);
        return {
            message: "Failed to create launchpad",
            error: error instanceof Error ? error.message : error,
        };
    }
}

export default createLaunchpad;