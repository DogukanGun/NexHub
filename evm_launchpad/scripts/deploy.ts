import { ethers } from "hardhat";

async function main() {
  // Get signers
  const [deployer, signer] = await ethers.getSigners();

  console.log("Deploying contracts with the account:", deployer.address);
  console.log("Account balance:", (await ethers.provider.getBalance(deployer.address)).toString());

  // Deploy mock ERC20 token for testing
  const ERC20MockFactory = await ethers.getContractFactory("ERC20Mock");
  const mockToken = await ERC20MockFactory.deploy(
    "NexHubToken", 
    "NHT", 
    deployer.address, 
    ethers.parseEther("1000000")
  );
  await mockToken.waitForDeployment();
  console.log("Mock Token deployed to:", mockToken.target);

  // Deploy LaunchpadFactory
  const LaunchpadFactoryFactory = await ethers.getContractFactory("LaunchpadFactory");
  const launchpadFactory = await LaunchpadFactoryFactory.deploy(deployer.address);
  await launchpadFactory.waitForDeployment();
  console.log("LaunchpadFactory deployed to:", launchpadFactory.target);

  // Create first Launchpad instance
  const createLaunchpadTx = await launchpadFactory.createLaunchpad(
    mockToken.target,
    signer.address,
    "NexHubLaunchpad",
    "1.0"
  );
  const createLaunchpadReceipt = await createLaunchpadTx.wait();
  
  // Get the deployed Launchpad address from events
  const launchpadCreatedEvent = createLaunchpadReceipt?.logs.find(
    log => log.topics[0] === ethers.id("LaunchpadCreated(address,address,address)")
  );
  
  if (launchpadCreatedEvent) {
    const launchpadAddress = `0x${launchpadCreatedEvent.topics[1].slice(-40)}`;
    console.log("First Launchpad deployed to:", launchpadAddress);

    // Transfer some tokens to the Launchpad for claims
    const LaunchpadFactory = await ethers.getContractFactory("Launchpad");
    const launchpad = LaunchpadFactory.attach(launchpadAddress);
    
    const transferAmount = ethers.parseEther("100000");
    await mockToken.transfer(launchpadAddress, transferAmount);
    console.log(`Transferred ${ethers.formatEther(transferAmount)} tokens to Launchpad`);
  }

  console.log("Deployment complete!");
}

// Recommended pattern for handling async errors
main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  }); 