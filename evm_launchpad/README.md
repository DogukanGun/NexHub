# NexHub Launchpad Smart Contracts

## Overview

NexHub Launchpad is a secure, flexible token distribution system built on Ethereum, utilizing EIP-712 signature verification and a factory pattern for creating multiple launchpad instances.

## Features

🔒 Secure Token Claims
- EIP-712 Signature Verification
- ECDSA Signature Validation
- Claim Expiration Mechanism
- Per-Round Claim Tracking

🏭 Dynamic Launchpad Creation
- Factory Pattern for Multiple Launchpads
- Centralized Management
- Owner-Controlled Validation

## Contracts

### Launchpad
Handles token claims using off-chain signatures.

#### Key Functions
- `claim(uint256 allowedAmount, uint256 roundId, uint256 deadline, bytes signature)`: 
  Allows users to claim tokens with a valid signature

### LaunchpadFactory
Manages the creation and lifecycle of Launchpad instances.

#### Key Functions
- `createLaunchpad(IERC20 token, address signer, string name, string version)`: 
  Creates a new Launchpad
- `getAllLaunchpads()`: 
  Retrieves all created Launchpad addresses
- `invalidateLaunchpad(address launchpadAddress)`: 
  Invalidates a specific Launchpad

## Prerequisites

- Node.js (v16+ recommended)
- npm or Yarn
- Hardhat

## Installation

1. Clone the repository
```bash
git clone https://github.com/your-org/nexhub-launchpad.git
cd nexhub-launchpad
```

2. Install dependencies
```bash
npm install
# or
yarn install
```

## Development

### Compile Contracts
```bash
npm run compile
# or
yarn compile
```

### Run Tests
```bash
npm test
# or
yarn test
```

### Test Coverage
```bash
npm run test:coverage
# or
yarn test:coverage
```

## Deployment

### Local Deployment
```bash
npm run deploy:local
# or
yarn deploy:local
```

### Testnet Deployment
```bash
npm run deploy:testnet
# or
yarn deploy:testnet
```

### Mainnet Deployment
```bash
npm run deploy:mainnet
# or
yarn deploy:mainnet
```

## Backend Signature Example

```typescript
const domain = {
  name: 'NexHubLaunchpad',
  version: '1.0',
  chainId: 1, // Mainnet
  verifyingContract: launchpadAddress
};

const types = {
  Claim: [
    { name: 'user', type: 'address' },
    { name: 'allowedAmount', type: 'uint256' },
    { name: 'roundId', type: 'uint256' },
    { name: 'deadline', type: 'uint256' }
  ]
};

const signature = await signer._signTypedData(
  domain, 
  types, 
  { user, allowedAmount, roundId, deadline }
);
```

## Security Considerations

- Signatures are bound to specific users and claim parameters
- Claims can only be used once per round
- Configurable claim expiration
- Owner-controlled signer management

## Dependencies

- Solidity ^0.8.20
- OpenZeppelin Contracts ^5.0.0

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Audit Status

⚠️ These contracts should undergo a professional security audit before mainnet deployment.

## Contact

NexHub Team - [Your Contact Information]

Project Link: [https://github.com/your-org/nexhub-launchpad](https://github.com/your-org/nexhub-launchpad)
