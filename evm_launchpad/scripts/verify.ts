import { run } from "hardhat";
import * as fs from "fs";
import * as path from "path";

async function main() {
  // Read deployment addresses from file
  const deploymentPath = path.join(__dirname, "../deployments.json");
  if (!fs.existsSync(deploymentPath)) {
    throw new Error("Deployment file not found. Please run deploy.ts first");
  }

  const deployments = JSON.parse(fs.readFileSync(deploymentPath, "utf8"));
  console.log("Loaded deployments:", deployments);

  console.log("Starting contract verification...");

  try {
    // Verify Mock USDC
    console.log("\nVerifying Mock USDC at", deployments.mockUSDC);
    await run("verify:verify", {
      address: deployments.mockUSDC,
      contract: "contracts/mocks/ERC20Mock.sol:ERC20Mock",
      constructorArguments: [
        "USD Coin",
        "USDC",
        deployments.deployer,
        "1000000000000" // 1,000,000 USDC with 6 decimals
      ],
    });
    console.log("Mock USDC verified successfully");

    // Verify Project Token
    console.log("\nVerifying Project Token at", deployments.projectToken);
    await run("verify:verify", {
      address: deployments.projectToken,
      contract: "contracts/mocks/ERC20Mock.sol:ERC20Mock",
      constructorArguments: [
        "NexHubToken",
        "NHT",
        deployments.deployer,
        "1000000000000000000000000" // 1,000,000 tokens with 18 decimals
      ],
    });
    console.log("Project Token verified successfully");

    // Verify LaunchpadFactory
    console.log("\nVerifying LaunchpadFactory at", deployments.launchpadFactory);
    await run("verify:verify", {
      address: deployments.launchpadFactory,
      contract: "contracts/LaunchpadFactory.sol:LaunchpadFactory",
      constructorArguments: [
        deployments.deployer,
        deployments.mockUSDC
      ],
    });
    console.log("LaunchpadFactory verified successfully");

    console.log("\nAll contracts verified successfully!");
  } catch (error: any) {
    if (error.message.includes("Already Verified")) {
      console.log("Contract is already verified!");
    } else {
      console.error("Error during verification:", error);
      throw error;
    }
  }
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  }); 