import { expect } from "chai";
import { ethers } from "hardhat";
import { SignerWithAddress } from "@nomicfoundation/hardhat-ethers/signers";
import { Launchpad, ERC20Mock } from "../typechain-types";
import { TypedDataDomain, TypedDataField } from "ethers";

describe("LaunchpadClaim", function () {
  let launchpadClaim: Launchpad;
  let mockToken: ERC20Mock;
  let owner: SignerWithAddress;
  let signer: SignerWithAddress;
  let user: SignerWithAddress;

  const CLAIM_TYPEHASH = ethers.keccak256(
    ethers.toUtf8Bytes("Claim(address user,uint256 allowedAmount,uint256 roundId,uint256 deadline)")
  );

  beforeEach(async function () {
    [owner, signer, user] = await ethers.getSigners();

    // Deploy mock ERC20 token
    const ERC20MockFactory = await ethers.getContractFactory("ERC20Mock");
    mockToken = await ERC20MockFactory.connect(owner).deploy("MockToken", "MTK", owner.address, 1000000);

    // Deploy LaunchpadClaim
    const LaunchpadFactory = await ethers.getContractFactory("Launchpad");
    launchpadClaim = await LaunchpadFactory.connect(owner).deploy(
      mockToken.target, 
      signer.address, 
      "Launchpad", 
      "1.0"
    );

    // Mint tokens to the contract
    await mockToken.connect(owner).transfer(launchpadClaim.target, 500000);
  });

  async function generateSignature(
    signerAccount: SignerWithAddress, 
    userAddress: string, 
    allowedAmount: bigint, 
    roundId: number, 
    deadline: number
  ) {
    const domain: TypedDataDomain = {
        name: "Launchpad",
      version: "1.0",
      chainId: (await ethers.provider.getNetwork()).chainId,
      verifyingContract: launchpadClaim.target as string
    };

    const types: Record<string, TypedDataField[]> = {
      Claim: [
        { name: "user", type: "address" },
        { name: "allowedAmount", type: "uint256" },
        { name: "roundId", type: "uint256" },
        { name: "deadline", type: "uint256" }
      ]
    };

    const message = {
      user: userAddress,
      allowedAmount,
      roundId,
      deadline
    };

    return signerAccount.signTypedData(domain, types, message);
  }

  it("Should allow valid claim", async function () {
    const allowedAmount = ethers.parseEther("100");
    const roundId = 1;
    const deadline = Math.floor(Date.now() / 1000) + 3600; // 1 hour from now

    const signature = await generateSignature(
      signer, 
      user.address, 
      allowedAmount, 
      roundId, 
      deadline
    );

    await expect(
      launchpadClaim.connect(user).claim(
        allowedAmount, 
        roundId, 
        deadline, 
        signature
      )
    ).to.emit(launchpadClaim, "TokenClaimed")
      .withArgs(user.address, allowedAmount, roundId);

    // Check token balance
    expect(await mockToken.balanceOf(user.address)).to.equal(allowedAmount);

    // Check claimed status
    expect(await launchpadClaim.hasClaimed(user.address, roundId)).to.be.true;
  });

  it("Should prevent duplicate claims", async function () {
    const allowedAmount = ethers.parseEther("100");
    const roundId = 1;
    const deadline = Math.floor(Date.now() / 1000) + 3600;

    const signature = await generateSignature(
      signer, 
      user.address, 
      allowedAmount, 
      roundId, 
      deadline
    );

    // First claim should succeed
    await launchpadClaim.connect(user).claim(
      allowedAmount, 
      roundId, 
      deadline, 
      signature
    );

    // Second claim should fail
    await expect(
      launchpadClaim.connect(user).claim(
        allowedAmount, 
        roundId, 
        deadline, 
        signature
      )
    ).to.be.revertedWith("Already claimed");
  });

  it("Should prevent expired claims", async function () {
    const allowedAmount = ethers.parseEther("100");
    const roundId = 1;
    const deadline = Math.floor(Date.now() / 1000) - 3600; // Expired deadline

    const signature = await generateSignature(
      signer, 
      user.address, 
      allowedAmount, 
      roundId, 
      deadline
    );

    await expect(
      launchpadClaim.connect(user).claim(
        allowedAmount, 
        roundId, 
        deadline, 
        signature
      )
    ).to.be.revertedWith("Claim has expired");
  });

  it("Should prevent invalid signatures", async function () {
    const allowedAmount = ethers.parseEther("100");
    const roundId = 1;
    const deadline = Math.floor(Date.now() / 1000) + 3600;

    // Signature from wrong signer
    const invalidSignature = await generateSignature(
      user, 
      user.address, 
      allowedAmount, 
      roundId, 
      deadline
    );

    await expect(
      launchpadClaim.connect(user).claim(
        allowedAmount, 
        roundId, 
        deadline, 
        invalidSignature
      )
    ).to.be.revertedWith("Invalid signature");
  });

  it("Should allow owner to update signer", async function () {
    const newSigner = user;

    await expect(
      launchpadClaim.connect(owner).updateSigner(newSigner.address)
    ).to.emit(launchpadClaim, "SignerUpdated")
      .withArgs(signer.address, newSigner.address);

  });
}); 