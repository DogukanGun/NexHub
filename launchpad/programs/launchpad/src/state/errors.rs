use anchor_lang::prelude::*;

#[error_code]
pub enum ErrorCode {
    #[msg("Launchpad is not active")]
    LaunchpadNotActive,
    #[msg("Insufficient tokens in vault")]
    InsufficientTokens,
    #[msg("Math overflow occurred")]
    MathOverflow,
}
