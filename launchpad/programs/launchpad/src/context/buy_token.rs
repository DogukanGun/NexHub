use anchor_lang::prelude::*;
use anchor_spl::token::{Mint, Token, TokenAccount, transfer, Transfer};
use anchor_spl::associated_token::AssociatedToken;

use crate::state::{
    launchpad::LaunchpadState, 
    errors::ErrorCode
};

#[derive(Accounts)]
#[instruction(launchpad_name: String)]
pub struct BuyToken<'info> {
    #[account(mut)]
    pub buyer: Signer<'info>,

    #[account(mut)]
    pub signer: Signer<'info>,

    /// CHECK: This is the launchpad owner
    #[account(mut)]
    pub launchpad_owner: AccountInfo<'info>,

    #[account(
        mut,
        seeds = [maker_token.key().as_ref(), launchpad_name.as_ref(), b"launchpad_state"],
        bump = launchpad_state.bump,
        constraint = launchpad_state.token_mint == maker_token.key(),
        constraint = launchpad_state.is_active == true
    )]
    pub launchpad_state: Box<Account<'info, LaunchpadState>>,


    #[account(
        mut,
        seeds = [maker_token.key().as_ref(), launchpad_name.as_ref()],
        bump = launchpad_state.token_vault_bump,
        token::mint = maker_token,
        token::authority = signer,
    )]
    pub token_vault: Account<'info, TokenAccount>,

    #[account(
        seeds = [launchpad_owner.key().as_ref(), launchpad_name.as_ref()],
        bump = launchpad_state.token_bump,
        mint::decimals = 9,
        mint::authority = launchpad_owner,
    )]
    pub maker_token: Box<Account<'info, Mint>>,

    #[account(
        mut,
        associated_token::mint = maker_token,
        associated_token::authority = buyer,
    )]
    pub buyer_token_account: Account<'info, TokenAccount>,

    #[account(
        mut,
        seeds = [b"nex_token"],
        bump,
        mint::decimals = 9,
        mint::authority = signer,
    )]
    pub nex_token: Box<Account<'info, Mint>>,

    #[account(
        mut,
        seeds = [launchpad_name.as_bytes(), b"vault_nex"],
        bump,
        token::mint = nex_token,
        token::authority = signer,
    )]
    pub vault_nex: Account<'info, TokenAccount>,

    #[account(
        mut,
        associated_token::mint = nex_token,
        associated_token::authority = buyer,
    )]
    pub buyer_nex_token_account: Account<'info, TokenAccount>,

    pub token_program: Program<'info, Token>,
    pub associated_token_program: Program<'info, AssociatedToken>,
    pub system_program: Program<'info, System>,
}

impl<'info> BuyToken<'info> {
    pub fn buy_tokens(&mut self, launchpad_name: String, amount_to_buy: u64) -> Result<()> {
        // Exchange rate: 1 A token = 2 NexTokens
        let nex_tokens_required = self.launchpad_state.token_price
            .checked_mul(amount_to_buy)
            .ok_or(ErrorCode::MathOverflow)?;

        // Check if buyer has enough NexTokens
        require!(
            self.buyer_nex_token_account.amount >= nex_tokens_required, 
            ErrorCode::InsufficientTokens
        );

        // Check if token vault has enough tokens
        require!(
            self.token_vault.amount >= amount_to_buy, 
            ErrorCode::InsufficientTokens
        );

        msg!("checks are done");

        // Transfer NexTokens from buyer to NexToken vault
        let nex_transfer_accounts = Transfer {
            from: self.buyer_nex_token_account.to_account_info(),
            to: self.vault_nex.to_account_info(),
            authority: self.buyer.to_account_info(),
        };
        let nex_transfer_ctx = CpiContext::new(
            self.token_program.to_account_info(), 
            nex_transfer_accounts
        );
        transfer(nex_transfer_ctx, nex_tokens_required)?;

        msg!("NexTokens transferred");

        // Transfer A tokens from vault to buyer
        let a_token_transfer_accounts = Transfer {
            from: self.token_vault.to_account_info(),
            to: self.buyer_token_account.to_account_info(),
            authority: self.signer.to_account_info(),
        };
        let a_token_transfer_ctx = CpiContext::new(
            self.token_program.to_account_info(), 
            a_token_transfer_accounts
        );

        msg!("Launchpad tokens transferred");
        
        let maker_token_key = self.maker_token.key();
        // Use PDA as signer
        let seeds = &[
            maker_token_key.as_ref(), 
            launchpad_name.as_ref(), 
            b"launchpad_state",
            &[self.launchpad_state.bump]
        ];
        transfer(a_token_transfer_ctx.with_signer(&[seeds]), amount_to_buy)?;

        // Update total raised
        self.launchpad_state.total_raised = self.launchpad_state.total_raised
            .checked_add(nex_tokens_required)
            .ok_or(ErrorCode::MathOverflow)?;

        Ok(())
    }
}