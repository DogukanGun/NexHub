use anchor_lang::prelude::*;
pub mod context;
pub mod state;
pub use context::*;
pub use state::*;

declare_id!("4Ggn6GWu5uK9FJsVj1dUmDfGebf1pPeKjqcv2QqdFyeg");

#[program]
pub mod launchpad {
    use super::*;

    pub fn init_launchpad(
        ctx: Context<InitLaunchpad>, 
        launchpad_name: String,
        token_price: u64, 
        token_story_id: u64, 
        total_supply: u64
    ) -> Result<()> {
        ctx.accounts.init_launchpad(launchpad_name, token_price, token_story_id, total_supply, &ctx.bumps)
    }

    pub fn buy_token(
        ctx: Context<BuyToken>, 
        launchpad_name: String,
        amount_to_buy: u64
    ) -> Result<()> {
        ctx.accounts.buy_tokens(launchpad_name, amount_to_buy)
    }
}