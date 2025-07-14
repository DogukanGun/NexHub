// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import {Launchpad} from "./Launchpad.sol";
import {IERC20} from "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import {Ownable} from "@openzeppelin/contracts/access/Ownable.sol";
import {SafeERC20} from "@openzeppelin/contracts/token/ERC20/utils/SafeERC20.sol";

/**
 * @title LaunchpadFactory
 * @notice Factory contract for creating and managing Launchpad instances
 * @dev Allows creation of multiple Launchpad contracts with different configurations
 */
contract LaunchpadFactory is Ownable {
    using SafeERC20 for IERC20;

    // Events
    event LaunchpadCreated(
        address indexed launchpad, 
        address indexed token, 
        address indexed signer
    );

    // Storage
    address[] public launchpads;
    mapping(address => bool) public isLaunchpadValid;
    IERC20 public usdcToken;

    // NEW: Super admin to receive fees
    address public superAdmin;

    // NEW: Fee structure (0.001% = 1 / 100,000)
    uint256 public constant FEE_NUMERATOR = 1;
    uint256 public constant FEE_DENOMINATOR = 100000;

    // NEW: Deployment fee in USDC (39 USDC with 6 decimals)
    uint256 private constant DEPLOYMENT_FEE = 39 * 10**6;

    /**
     * @notice Constructor to set initial owner
     * @param initialOwner Address to be set as the contract owner
     */
    constructor(address initialOwner, IERC20 _usdcToken) Ownable(initialOwner) {
        usdcToken = _usdcToken;
        superAdmin = initialOwner; // For simplicity, setting superAdmin to initialOwner
    }

    /**
     * @notice Create a new Launchpad instance
     * @param token ERC20 token to be distributed
     * @param signer Address authorized to sign claims
     * @param name EIP-712 domain name
     * @param version EIP-712 domain version
     * @return launchpadAddress Address of the newly created Launchpad
     */
    function createLaunchpad(
        IERC20 token, 
        address signer, 
        string memory name, 
        string memory version
    ) external returns (address launchpadAddress) {
        // Validate inputs
        require(address(token) != address(0), "Invalid token address");
        require(signer != address(0), "Invalid signer address");
        require(bytes(name).length > 0, "Invalid domain name");
        require(bytes(version).length > 0, "Invalid domain version");

        // Create Launchpad instance
        Launchpad launchpad = new Launchpad(
            token, 
            signer, 
            name, 
            version
        );

        // NEW: Charge 39 USDC deployment fee from the contract deployer
        // The deployer (msg.sender) must approve this contract to spend their USDC first
        usdcToken.safeTransferFrom(msg.sender, superAdmin, DEPLOYMENT_FEE);

        // Transfer ownership to the factory owner
        launchpad.transferOwnership(owner());

        // Store launchpad details
        launchpadAddress = address(launchpad);
        launchpads.push(launchpadAddress);
        isLaunchpadValid[launchpadAddress] = true;

        // Emit creation event
        emit LaunchpadCreated(launchpadAddress, address(token), signer);

        return launchpadAddress;
    }

    /**
     * @notice Get total number of created Launchpads
     * @return Number of Launchpad instances
     */
    function getLaunchpadCount() external view returns (uint256) {
        return launchpads.length;
    }

    /**
     * @notice Get all created Launchpad addresses
     * @return Array of Launchpad addresses
     */
    function getAllLaunchpads() external view returns (address[] memory) {
        return launchpads;
    }

    /**
     * @notice Invalidate a specific Launchpad
     * @param launchpadAddress Address of the Launchpad to invalidate
     */
    function invalidateLaunchpad(address launchpadAddress) external onlyOwner {
        require(isLaunchpadValid[launchpadAddress], "Launchpad not found");
        isLaunchpadValid[launchpadAddress] = false;
    }
}
