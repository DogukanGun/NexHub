use anchor_lang::prelude::*;
use anchor_spl::token::{Mint, Token, TokenAccount, mint_to, MintTo};

use crate::state::launchpad::LaunchpadState;

#[derive(Accounts)]
#[instruction(launchpad_name: String)]
pub struct InitLaunchpad<'info> {
    #[account(mut)]
    pub signer: Signer<'info>,

    #[account(mut)]
    pub launchpad_owner: Signer<'info>,

    #[account(
        init_if_needed,
        payer = launchpad_owner,
        mint::decimals = 9,
        mint::authority = launchpad_owner,
        seeds = [launchpad_owner.key().as_ref(), launchpad_name.as_ref()],
        bump,
    )]
    pub maker_token: Box<Account<'info, Mint>>,

    #[account(
        init_if_needed,
        payer = signer,
        seeds = [b"nex_token"],
        bump,
        mint::decimals = 9,
        mint::authority = signer
    )]
    pub nex_token: Box<Account<'info, Mint>>,

    #[account(
        init_if_needed,
        payer = launchpad_owner,
        seeds = [maker_token.key().as_ref(), launchpad_name.as_ref(), b"launchpad_state"],
        bump,
        space = 8 + LaunchpadState::INIT_SPACE
    )]
    pub launchpad_state: Box<Account<'info, LaunchpadState>>,

    #[account(
        init_if_needed,
        payer = launchpad_owner,
        seeds = [maker_token.key().as_ref(), launchpad_name.as_ref()],
        bump,
        token::mint = maker_token,
        token::authority = signer
    )]
    pub vault: Account<'info, TokenAccount>,

    #[account(
        init_if_needed,
        payer = launchpad_owner,
        seeds = [launchpad_name.as_bytes(), b"vault_nex"],
        bump,
        token::mint = nex_token,
        token::authority = signer
    )]
    pub vault_nex: Account<'info, TokenAccount>,

    pub token_program: Program<'info, Token>,
    pub system_program: Program<'info, System>,
}

impl<'info> InitLaunchpad<'info>  {
    pub fn init_launchpad(&mut self, launchpad_name: String, token_price: u64, token_story_id: u64, total_supply: u64, bump: &InitLaunchpadBumps) -> Result<()> {
        // Mint tokens to the vault
        let mint_accounts = MintTo {
            mint: self.maker_token.to_account_info(),
            to: self.vault.to_account_info(),
            authority: self.launchpad_owner.to_account_info(),
        };
        let token_program = self.token_program.to_account_info();
        let mint_ctx = CpiContext::new(token_program.clone(), mint_accounts);
        
        // Mint the total supply to the vault
        mint_to(mint_ctx, total_supply)?;

        // Initialize launchpad state
        self.launchpad_state.set_inner(LaunchpadState {
            creator: self.signer.key(),
            launchpad_name,
            admin: self.launchpad_owner.key(),
            total_raised: 0,
            token_bump: bump.maker_token,
            is_active: true,
            bump: bump.launchpad_state,
            token_price,
            token_story_id,
            token_mint: self.maker_token.key(),
            token_vault_bump: bump.vault,
            token_vault_authority: self.vault.key(),
            token_vault_authority_bump: bump.vault,
        });

        Ok(())
    }
}