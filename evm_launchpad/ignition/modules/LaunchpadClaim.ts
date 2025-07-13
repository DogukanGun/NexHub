import { buildModule } from "@nomicfoundation/hardhat-ignition/modules";

const LaunchpadClaimModule = buildModule("LaunchpadClaimModule", (m) => {
  // Example token address (replace with actual token address)
  const tokenAddress = m.getParameter("tokenAddress", "0x0000000000000000000000000000000000000000");
  
  // Example signer address (replace with actual signer address)
  const signerAddress = m.getParameter("signerAddress", "0x0000000000000000000000000000000000000000");

  const launchpadClaim = m.contract("LaunchpadClaim", [
    tokenAddress,
    signerAddress,
    "LaunchpadClaim",  // Domain name
    "1.0"             // Domain version
  ]);

  return { launchpadClaim };
});

export default LaunchpadClaimModule; 