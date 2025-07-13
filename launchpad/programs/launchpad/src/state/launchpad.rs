use anchor_lang::prelude::*;

#[account]
pub struct LaunchpadState {
    pub creator: Pubkey,
    pub launchpad_name: String,
    pub admin: Pubkey,
    pub total_raised: u64,
    pub is_active: bool,
    pub bump: u8,
    pub token_price: u64,
    pub token_story_id: u64,
    pub token_bump: u8,
    pub token_mint: Pubkey,
    pub token_vault_bump: u8,
    pub token_vault_authority: Pubkey,
    pub token_vault_authority_bump: u8,
}

impl LaunchpadState {
    pub const INIT_SPACE: usize = 8 + 32 + 32 + 8 + 1 + 8 + 32 + 1 + 8 + 32 + 1 + 8 + 32 + 1;
}