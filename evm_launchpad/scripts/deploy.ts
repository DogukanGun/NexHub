import { ethers } from "hardhat";
import * as fs from "fs";
import * as path from "path";

async function main() {
  // Get signers
  const [deployer] = await ethers.getSigners();

  console.log("Deploying contracts with the account:", deployer.address);
  console.log("Account balance:", (await ethers.provider.getBalance(deployer.address)).toString());

  // Deploy mock USDC token for testing
  const ERC20MockFactory = await ethers.getContractFactory("ERC20Mock");
  const mockUSDC = await ERC20MockFactory.deploy(
    "USD Coin", 
    "USDC", 
    deployer.address, 
    ethers.parseUnits("1000000", 6) // USDC has 6 decimals
  );
  await mockUSDC.waitForDeployment();
  console.log("Mock USDC deployed to:", mockUSDC.target);

  // Deploy project token
  const projectToken = await ERC20MockFactory.deploy(
    "NexHubToken", 
    "NHT", 
    deployer.address, 
    ethers.parseEther("1000000") // Project token uses 18 decimals
  );
  await projectToken.waitForDeployment();
  console.log("Project Token deployed to:", projectToken.target);

  // Deploy LaunchpadFactory with USDC
  const LaunchpadFactoryFactory = await ethers.getContractFactory("LaunchpadFactory");
  const launchpadFactory = await LaunchpadFactoryFactory.deploy(deployer.address, mockUSDC.target);
  await launchpadFactory.waitForDeployment();
  console.log("LaunchpadFactory deployed to:", launchpadFactory.target);

  // Approve LaunchpadFactory to spend USDC for deployment fee (39 USDC)
  const deploymentFee = ethers.parseUnits("39", 6); // USDC has 6 decimals
  await mockUSDC.approve(launchpadFactory.target, deploymentFee);
  console.log("Approved LaunchpadFactory to spend USDC");

  // Save deployment information
  const deployments = {
    network: (await ethers.provider.getNetwork()).name,
    deployer: deployer.address,
    mockUSDC: mockUSDC.target,
    projectToken: projectToken.target,
    launchpadFactory: launchpadFactory.target
  };

  // Save to file
  const deploymentPath = path.join(__dirname, "../deployments.json");
  fs.writeFileSync(
    deploymentPath,
    JSON.stringify(deployments, null, 2)
  );
  console.log(`Deployment addresses saved to ${deploymentPath}`);

  // Log all important addresses
  console.log("\nDeployment Summary:");
  console.log("-------------------");
  console.log("Mock USDC:", mockUSDC.target);
  console.log("Project Token:", projectToken.target);
  console.log("LaunchpadFactory:", launchpadFactory.target);
  console.log("Deployer:", deployer.address);
  console.log("\nDeployment complete!");
}

// Recommended pattern for handling async errors
main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  }); 