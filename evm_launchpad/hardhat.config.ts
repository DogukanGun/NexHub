import { HardhatUserConfig } from "hardhat/config";
import "@nomicfoundation/hardhat-toolbox";
import "@typechain/hardhat";
import * as dotenv from "dotenv";

// Load environment variables from .env file
dotenv.config();

// Ensure required environment variables are present
const BSCSCAN_API_KEY = process.env.BSCSCAN_API_KEY || "";
const PRIVATE_KEY = process.env.PRIVATE_KEY || "";
const BNB_TESTNET_RPC = process.env.BNB_TESTNET_RPC || "https://data-seed-prebsc-1-s1.binance.org:8545";
const BNB_MAINNET_RPC = process.env.BNB_MAINNET_RPC || "https://bsc-dataseed1.binance.org";

const config: HardhatUserConfig = {
  solidity: {
    version: "0.8.20",
    settings: {
      optimizer: {
        enabled: true,
        runs: 200
      }
    }
  },
  paths: {
    sources: "./contracts",
    tests: "./test",
    cache: "./cache",
    artifacts: "./artifacts"
  },
  typechain: {
    outDir: "typechain-types",
    target: "ethers-v6"
  },
  networks: {
    hardhat: {
      chainId: 31337
    },
    bscTestnet: {
      url: BNB_TESTNET_RPC,
      chainId: 97,
      accounts: [PRIVATE_KEY],
      gasPrice: 10000000000 // 10 Gwei
    },
    bsc: {
      url: BNB_MAINNET_RPC,
      chainId: 56,
      accounts: [PRIVATE_KEY],
      gasPrice: 3000000000 // 3 Gwei
    }
  },
  etherscan: {
    // Your API key for BSCScan
    // Obtain one at https://bscscan.com/
    apiKey: {
      bscTestnet: BSCSCAN_API_KEY,
      bsc: BSCSCAN_API_KEY
    }
  }
};

export default config;
