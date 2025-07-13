import * as anchor from "@coral-xyz/anchor";
import { Program } from "@coral-xyz/anchor";
import { Launchpad } from "../target/types/launchpad";
import { PublicKey, Keypair, SystemProgram } from "@solana/web3.js";
import * as token from "@solana/spl-token";
import { assert } from "chai";
import { createAccount, createMint, mintTo, getOrCreateAssociatedTokenAccount } from "@solana/spl-token";

describe("launchpad", () => {
  // Configure the client to use the local cluster.
  const provider = anchor.AnchorProvider.env();
  anchor.setProvider(provider);

  const program = anchor.workspace.Launchpad as Program<Launchpad>;

  // Test wallets
  const signer = Keypair.generate();
  const launchpadOwner = Keypair.generate();
  const buyer = Keypair.generate();

  // Launchpad parameters
  const launchpadName = "TestLaunchpad";
  const tokenPrice = new anchor.BN(2); // 2 NexTokens per A token
  const tokenStoryId = new anchor.BN(1);
  const totalSupply = new anchor.BN(1000000); // 1 million tokens

  // Derive PDAs
  let [makerToken, makerTokenBump] = PublicKey.findProgramAddressSync(
    [launchpadOwner.publicKey.toBuffer(), Buffer.from(launchpadName)],
    program.programId
  );

  let [launchpadState, launchpadStateBump] = PublicKey.findProgramAddressSync(
    [makerToken.toBuffer(), Buffer.from(launchpadName), Buffer.from("launchpad_state")],
    program.programId
  );

  let [vault, vaultBump] = PublicKey.findProgramAddressSync(
    [makerToken.toBuffer(), Buffer.from(launchpadName)],
    program.programId
  );

  let [vaultNex, vaultNexBump] = PublicKey.findProgramAddressSync(
    [Buffer.from(launchpadName + "vault_nex")],
    program.programId
  );

  let [nexToken, nexTokenBump] = PublicKey.findProgramAddressSync(
    [Buffer.from("nex_token")],
    program.programId
  );

  const confirm = async (signature: string): Promise<string> => {
    const block = await provider.connection.getLatestBlockhash();
    const res = await provider.connection.confirmTransaction({
      signature,
      ...block,
    });
    return signature;
  };

  const newMintToAta = async (connection, minter: Keypair, decimals: number = 9): Promise<{ mint: PublicKey, ata: PublicKey }> => {
    const mint = await createMint(connection, minter, minter.publicKey, null, decimals)
    const ata = await createAccount(connection, minter, mint, minter.publicKey)
    const signature = await mintTo(connection, minter, mint, ata, minter, 21e8)
    await confirm(signature)
    return {
      mint,
      ata
    }
  }

  beforeEach(async () => {
    // Airdrop SOL to test accounts
    const airdropBuyer = await provider.connection.requestAirdrop(buyer.publicKey, 10 * anchor.web3.LAMPORTS_PER_SOL);
    const airdropSigner = await provider.connection.requestAirdrop(signer.publicKey, 10 * anchor.web3.LAMPORTS_PER_SOL);
    const airdropLaunchpadOwner = await provider.connection.requestAirdrop(launchpadOwner.publicKey, 10 * anchor.web3.LAMPORTS_PER_SOL);
    
    await confirm(airdropBuyer);
    await confirm(airdropSigner);
    await confirm(airdropLaunchpadOwner);
    
    console.log("\nAirdropped 10 SOL to accounts");
  });

  it("Initialize Launchpad", async () => {
    // Create NexToken as a PDA
    const tx = await program.methods
      .initLaunchpad(
        launchpadName,
        tokenPrice,
        tokenStoryId,
        totalSupply
      )
      .accounts({
        signer: signer.publicKey,
        launchpadOwner: launchpadOwner.publicKey,
      })
      .signers([signer, launchpadOwner])
      .rpc();
    
    await confirm(tx);
    console.log("Your transaction signature", tx);
    
    // Fetch and verify launchpad state
    const launchpadStateAccount = await program.account.launchpadState.fetch(launchpadState);

    assert.equal(launchpadStateAccount.launchpadName, launchpadName);
    assert.equal(launchpadStateAccount.tokenPrice.toString(), tokenPrice.toString());
    assert.equal(launchpadStateAccount.isActive, true);
    assert.equal(launchpadStateAccount.totalRaised.toString(), "0");
  });

  it("Buy Tokens", async () => {
    // Fetch launchpad state to get admin and token details
    const launchpadStateAccount = await program.account.launchpadState.fetch(launchpadState);
    
    // Create buyer's token accounts
    const buyerMakerTokenAccount = await getOrCreateAssociatedTokenAccount(
      provider.connection,
      buyer,
      makerToken,
      buyer.publicKey
    );

    // Mint NexTokens to buyer
    const buyerNexTokenAccount = await getOrCreateAssociatedTokenAccount(
      provider.connection,
      buyer,
      nexToken,
      buyer.publicKey
    );

    console.log("signer", signer.publicKey.toBase58());
    console.log("buyer", buyer.publicKey.toBase58());
    console.log("nexToken", nexToken.toBase58());
    console.log("buyerNexTokenAccount", buyerNexTokenAccount.address.toBase58());
    await mintTo(
      provider.connection, 
      buyer,
      nexToken,
      buyerNexTokenAccount.address, 
      signer.publicKey, 
      1000000,
      [signer]
    );

    console.log("minted NexTokens to buyer");
    // Amount of tokens to buy
    const amountToBuy = new anchor.BN(100);

    // Buy tokens
    const tx = await program.methods
      .buyToken(
        launchpadName,
        amountToBuy
      )
      .accounts({
        buyer: buyer.publicKey,
        signer: signer.publicKey,
        launchpadOwner: launchpadStateAccount.admin,
      })
      .signers([buyer, signer])
      .rpc();

    await confirm(tx);

    // Fetch updated launchpad state
    const updatedLaunchpadState = await program.account.launchpadState.fetch(launchpadState);

    // Verify total raised (NexTokens)
    const expectedTotalRaised = tokenPrice.mul(amountToBuy);
    assert.equal(
      updatedLaunchpadState.totalRaised.toString(),
      expectedTotalRaised.toString()
    );

    // Verify buyer's token balance
    const buyerBalance = await provider.connection.getTokenAccountBalance(buyerMakerTokenAccount.address);
    assert.equal(buyerBalance.value.amount, amountToBuy.toString());

    // Verify NexToken vault balance
    const vaultNexBalance = await provider.connection.getTokenAccountBalance(vaultNex);
    assert.equal(vaultNexBalance.value.amount, expectedTotalRaised.toString());
  });
});
