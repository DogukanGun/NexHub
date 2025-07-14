// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import {ERC20} from "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import {Ownable} from "@openzeppelin/contracts/access/Ownable.sol";
import {EIP712} from "@openzeppelin/contracts/utils/cryptography/EIP712.sol";
import {ECDSA} from "@openzeppelin/contracts/utils/cryptography/ECDSA.sol";
import {SafeERC20} from "@openzeppelin/contracts/token/ERC20/utils/SafeERC20.sol";
import {IERC20} from "@openzeppelin/contracts/token/ERC20/IERC20.sol";

/**
 * @title Launchpad
 * @notice A contract for token claims using EIP-712 signatures with fee logic
 * @dev Implements secure token claims with signature verification and operational fees
 */
contract Launchpad is EIP712, Ownable {
    using SafeERC20 for IERC20;

    // --- Events ---
    event TokenClaimed(
        address indexed user, 
        uint256 amount, 
        uint256 roundId
    );
    event SignerUpdated(
        address indexed previousSigner, 
        address indexed newSigner
    );
    

    // --- Storage ---
    IERC20 public immutable claimableToken;
    address public signer;

    // Tracking claimed status per user and round
    mapping(address => mapping(uint256 => bool)) public hasClaimed;

    // --- Constants ---
    // EIP-712 type hash for claim verification
    bytes32 private constant CLAIM_TYPEHASH = 
        keccak256("Claim(address user,uint256 allowedAmount,uint256 roundId,uint256 deadline)");

    /**
     * @notice Constructor to initialize the contract
     * @param _token Address of the token to be claimed
     * @param _signer Initial signer address for signature verification
     * @param _name Domain name for EIP-712
     * @param _version Domain version for EIP-712
     */
    constructor(
        IERC20 _token, 
        address _signer, 
        string memory _name, 
        string memory _version
    ) EIP712(_name, _version) Ownable(msg.sender) {
        require(_signer != address(0), "Invalid signer address");
        
        claimableToken = _token;
        signer = _signer;
    }

    /**
     * @notice Claim tokens using a valid signature and pay an operational fee
     * @param allowedAmount Amount of tokens to claim
     * @param roundId Identifier for the claim round
     * @param deadline Timestamp after which the claim is invalid
     * @param signature Signed message authorizing the claim
     */
    function claim(
        uint256 allowedAmount, 
        uint256 roundId, 
        uint256 deadline, 
        bytes calldata signature
    ) external {
        require(block.timestamp <= deadline, "Claim has expired");
        require(!hasClaimed[msg.sender][roundId], "Already claimed");

        bytes32 digest = _hashTypedDataV4(keccak256(abi.encode(
            CLAIM_TYPEHASH,
            msg.sender,
            allowedAmount,
            roundId,
            deadline
        )));

        address recoveredSigner = ECDSA.recover(digest, signature);
        require(recoveredSigner == signer, "Invalid signature");

        hasClaimed[msg.sender][roundId] = true;

        // Transfer tokens to the user
        claimableToken.safeTransfer(msg.sender, allowedAmount);

        emit TokenClaimed(msg.sender, allowedAmount, roundId);
    }

    /**
     * @notice Allows the owner to withdraw remaining claimable tokens
     * @dev A 0.001% fee is sent to the super admin from the withdrawn amount.
     */
    function withdrawRemainingTokens() external onlyOwner {
        uint256 balance = claimableToken.balanceOf(address(this));
        require(balance > 0, "No tokens to withdraw");

        // Transfer the rest to the owner
        claimableToken.safeTransfer(owner(), balance);

    }

    /**
     * @notice Update the signer address
     * @param _newSigner New address authorized to sign claims
     */
    function updateSigner(address _newSigner) external onlyOwner {
        require(_newSigner != address(0), "Invalid signer address");
        
        address previousSigner = signer;
        signer = _newSigner;

        emit SignerUpdated(previousSigner, _newSigner);
    }

    /**
     * @notice Check if a user has claimed for a specific round
     * @param user Address to check
     * @param roundId Round identifier
     * @return Boolean indicating claim status
     */
    function checkClaimed(address user, uint256 roundId) external view returns (bool) {
        return hasClaimed[user][roundId];
    }
}