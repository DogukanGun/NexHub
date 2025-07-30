package com.dag.nexwallet.base.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dag.nexwallet.R

@Composable
fun getDestinationTitle(destination: String): String{
    return when(destination) {
        Destination.HomeScreen.toString() -> {
            stringResource(R.string.home_destination_title)
        }
        Destination.SolanaChat.toString()  -> {
            stringResource(R.string.solana_destination_title)
        }
        Destination.AIView.toString()  -> {
            stringResource(R.string.ai_destination_title)
        }
        Destination.ManifestScreen.toString() -> {
            stringResource(R.string.manifest_destination_title)
        }
        else -> {
            ""
        }
    }
}