// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import {Launchpad} from "./Launchpad.sol";
import {IERC20} from "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import {Ownable} from "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title LaunchpadFactory
 * @notice Factory contract for creating and managing Launchpad instances
 * @dev Allows creation of multiple Launchpad contracts with different configurations
 */
contract LaunchpadFactory is Ownable {
    // Events
    event LaunchpadCreated(
        address indexed launchpad, 
        address indexed token, 
        address indexed signer
    );

    // Storage
    address[] public launchpads;
    mapping(address => bool) public isLaunchpadValid;

    /**
     * @notice Constructor to set initial owner
     * @param initialOwner Address to be set as the contract owner
     */
    constructor(address initialOwner) Ownable(initialOwner) {}

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
