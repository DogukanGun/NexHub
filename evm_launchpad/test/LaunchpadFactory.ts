import { expect } from "chai";
import { ethers } from "hardhat";
import { SignerWithAddress } from "@nomicfoundation/hardhat-ethers/signers";
import { LaunchpadFactory, ERC20Mock, Launchpad } from "../typechain-types";

const { log } = console;

describe("LaunchpadFactory", function () {
  let launchpadFactory: LaunchpadFactory;
  let owner: SignerWithAddress;
  let user: SignerWithAddress;
  let signer: SignerWithAddress;
  let mockToken: ERC20Mock;
  let usdcToken: ERC20Mock;

  beforeEach(async function () {
    // Get signers
    [owner, user, signer] = await ethers.getSigners();

    // Deploy mock ERC20 token
    const ERC20MockFactory = await ethers.getContractFactory("ERC20Mock");
    mockToken = await ERC20MockFactory.connect(owner).deploy(
      "MockToken", 
      "MTK", 
      owner.address, 
      ethers.parseEther("1000000")
    );
    usdcToken = await ERC20MockFactory.connect(owner).deploy(
      "USDC", 
      "USDC", 
      owner.address, 
      ethers.parseEther("1000000")
    );

    // Deploy LaunchpadFactory
    const LaunchpadFactoryFactory = await ethers.getContractFactory("LaunchpadFactory");
    launchpadFactory = await LaunchpadFactoryFactory.connect(owner).deploy(owner.address, usdcToken);
      // Approve USDC for LaunchpadFactory
      // Approve the LaunchpadFactory to spend 39 USDC (with 6 decimals)
    await usdcToken.approve(await launchpadFactory.getAddress(), ethers.parseUnits("195", 6));

    log("Allowance after approval:", await usdcToken.allowance(owner.address, await launchpadFactory.getAddress()));

  });

  describe("Launchpad Creation", function () {
    it("Should create a new Launchpad instance", async function () {
      const sellerPrice = ethers.parseEther("10"); // Example seller price

      // Create Launchpad
      const tx = await launchpadFactory.createLaunchpad(
        mockToken.target, 
        signer.address, 
        sellerPrice
      );

      // Wait for transaction and get receipt
      const receipt = await tx.wait();

      // Check event was emitted
      const events = receipt?.logs.filter(
        log => log.topics[0] === ethers.id("LaunchpadCreated(address,address,address,uint256)")
      );
      expect(events).to.have.lengthOf(1);

      // Verify Launchpad count
      const launchpadCount = await launchpadFactory.getLaunchpadCount();
      expect(launchpadCount).to.equal(1);

      // Get created Launchpad addresses
      const launchpads = await launchpadFactory.getAllLaunchpads();
      expect(launchpads).to.have.lengthOf(1);

      // Verify Launchpad details
      const LaunchpadFactory = await ethers.getContractFactory("Launchpad");
      const launchpad = LaunchpadFactory.attach(launchpads[0]) as Launchpad;

      // Transfer mock tokens to the newly created Launchpad
      await mockToken.transfer(launchpad.target, ethers.parseEther("1000")); 

      expect(await launchpad.claimableToken()).to.equal(mockToken.target);
      expect(await launchpad.owner()).to.equal(owner.address);
    });

    it("Should prevent creating Launchpad with invalid inputs", async function () {
      const sellerPrice = ethers.parseEther("10"); // Example seller price

      // Zero address for token
      await expect(
        launchpadFactory.createLaunchpad(
          ethers.ZeroAddress, 
          signer.address, 
          sellerPrice
        )
      ).to.be.revertedWith("Invalid token address");

      // Zero address for signer
      await expect(
        launchpadFactory.createLaunchpad(
          mockToken.target, 
          ethers.ZeroAddress, 
          sellerPrice
        )
      ).to.be.revertedWith("Invalid signer address");

      // Zero seller price
      await expect(
        launchpadFactory.createLaunchpad(
          mockToken.target, 
          signer.address, 
          0
        )
      ).to.be.revertedWith("Invalid seller price");
    });

    it("Should allow creating multiple Launchpads", async function () {
      // Create multiple Launchpads
      const launchpadCount = 5;
      const sellerPrice = ethers.parseEther("10"); // Example seller price

      for (let i = 0; i < launchpadCount; i++) {
        await launchpadFactory.createLaunchpad(
          mockToken.target, 
          signer.address, 
          sellerPrice
        );
      }

      // Verify Launchpad count
      const count = await launchpadFactory.getLaunchpadCount();
      expect(count).to.equal(launchpadCount);
    });
  });

  describe("Launchpad Management", function () {
    let launchpadAddress: string;
    const sellerPrice = ethers.parseEther("10"); // Example seller price

    beforeEach(async function () {
      // Create a Launchpad for testing
      const tx = await launchpadFactory.createLaunchpad(
        mockToken.target, 
        signer.address, 
        sellerPrice
      );
      const receipt = await tx.wait();
      const launchpads = await launchpadFactory.getAllLaunchpads();
      launchpadAddress = launchpads[0];
    });

    it("Should allow owner to invalidate a Launchpad", async function () {
      // Verify Launchpad is initially valid
      expect(await launchpadFactory.isLaunchpadValid(launchpadAddress)).to.be.true;

      // Invalidate Launchpad
      await launchpadFactory.invalidateLaunchpad(launchpadAddress);

      // Verify Launchpad is now invalid
      expect(await launchpadFactory.isLaunchpadValid(launchpadAddress)).to.be.false;
    });

    it("Should prevent non-owner from invalidating a Launchpad", async function () {
      // Try to invalidate from a non-owner account
      await expect(
        launchpadFactory.connect(user).invalidateLaunchpad(launchpadAddress)
      ).to.be.revertedWithCustomError(launchpadFactory, "OwnableUnauthorizedAccount");
    });

    it("Should prevent invalidating a non-existent Launchpad", async function () {
      // Try to invalidate a non-existent Launchpad
      await expect(
        launchpadFactory.invalidateLaunchpad(ethers.ZeroAddress)
      ).to.be.revertedWith("Launchpad not found");
    });
  });
}); 