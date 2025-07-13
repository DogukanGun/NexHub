import { buildModule } from "@nomicfoundation/hardhat-ignition/modules";

const LaunchpadFactoryModule = buildModule("LaunchpadFactoryModule", (m) => {
  // Get the first signer (typically the deployer) as the initial owner
  const deployer = m.getParameter("deployer", "0x0000000000000000000000000000000000000000");

  // Deploy the LaunchpadFactory
  const launchpadFactory = m.contract("LaunchpadFactory", [
    deployer
  ]);

  return { launchpadFactory };
});

export default LaunchpadFactoryModule; 