package com.dag.nexwallet.features.solana.presentation

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.dag.nexwallet.base.BaseVM
import com.dag.nexwallet.base.components.bottomnav.BottomNavMessageManager
import com.dag.nexwallet.base.scroll.ScrollStateManager
import com.dag.nexwallet.data.repository.WalletRepository
import com.dag.nexwallet.features.solana.domain.usecase.StakeTokensUseCase
import com.dag.nexwallet.features.solana.domain.usecase.SwapTokensUseCase
import com.funkatronics.encoders.Base58
import com.solana.mobilewalletadapter.clientlib.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.dag.wallet.ISolanaWalletManager
import com.dag.wallet.SolanaWalletManager
import com.example.agent.SolanaAiAgent

@HiltViewModel
class SolanaVM @Inject constructor(
    private val scrollManager: ScrollStateManager,
    private val bottomNavManager: BottomNavMessageManager,
    private val swapTokensUseCase: SwapTokensUseCase,
    private val stakeTokensUseCase: StakeTokensUseCase,
    private val walletRepository: WalletRepository,
    private val walletManager: ISolanaWalletManager,

) : BaseVM<SolanaVS>(SolanaVS.Loading) {

    private var walletAdapter: MobileWalletAdapter? = null

    init {
        scrollManager.updateScrolling(true)
        initializeAgent()
        initializeWalletAdapter()
    }

    private fun initializeWalletAdapter() {
        if (walletAdapter == null) {
            val solanaUri = "https://nexwallet.com".toUri()
            val iconUri = "favicon.ico".toUri()
            val identityName = "NexWallet"

            walletAdapter = MobileWalletAdapter(
                ConnectionIdentity(
                    identityUri = solanaUri,
                    iconUri = iconUri,
                    identityName = identityName
                )
            )
            walletAdapter?.blockchain = Solana.Mainnet
        }
    }

    private fun checkWalletConnection() {
        viewModelScope.launch {
            val walletAddress = walletRepository.getAddress().getOrNull()
            val currentState = _viewState.value
            if (currentState is SolanaVS.Success) {
                _viewState.value = currentState.copy(
                    isWalletConnected = !walletAddress.isNullOrEmpty(),
                    showWalletConnectionDialog = walletAddress.isNullOrEmpty()
                )
            }
        }
    }

    fun connectWallet(sender: ActivityResultSender) {
        viewModelScope.launch {
            try {
                initializeWalletAdapter()
                
                walletAdapter?.let { adapter ->
                    when (val result = adapter.connect(sender)) {
                        is TransactionResult.Success -> {
                            val address = result.authResult.accounts.firstOrNull()?.publicKey?.let {
                                Base58.encodeToString(it)
                            }
                            
                            if (address != null) {
                                walletRepository.saveAddress(address)
                                val currentState = _viewState.value
                                if (currentState is SolanaVS.Success) {
                                    _viewState.value = currentState.copy(
                                        isWalletConnected = true,
                                        showWalletConnectionDialog = false
                                    )
                                }
                            }
                        }
                        is TransactionResult.NoWalletFound -> {
                            handleError("No compatible wallet found")
                        }
                        is TransactionResult.Failure -> {
                            handleError("Failed to connect wallet: ${result.e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                handleError("Failed to connect wallet: ${e.message}")
            }
        }
    }

    fun dismissWalletConnectionDialog() {
        val currentState = _viewState.value
        if (currentState is SolanaVS.Success) {
            _viewState.value = currentState.copy(showWalletConnectionDialog = false)
        }
    }

    private fun initializeAgent() {
        viewModelScope.launch {
            try {
                bottomNavManager.showMessage("Solana AI Agent")
                _viewState.value = SolanaVS.Success(
                    currentContext = SolanaVS.AgentContext(
                        connectedWallet = "",
                        solBalance = "0",
                        recentTokens = emptyList(),
                        recentDapps = emptyList()
                    ),
                    chatMessages = listOf(
                        SolanaVS.ChatMessage(
                            content = "Hi! I'm your Solana AI Agent. I can help you with transactions, token swaps, and connecting to dApps. What would you like to do?",
                            isFromAI = true
                        )
                    ),
                    suggestedActions = emptyList()
                )
            } catch (e: Exception) {
                _viewState.value = SolanaVS.Error(e.message ?: "Failed to initialize Solana Agent")
            }
        }
    }

    fun toggleHeader() {
        val currentState = _viewState.value
        if (currentState is SolanaVS.Success) {
            _viewState.value = currentState.copy(
                isHeaderExpanded = !currentState.isHeaderExpanded
            )
        }
    }

    fun sendMessage(content: String, activity: Activity?) {
        val currentState = _viewState.value
        if (currentState is SolanaVS.Success && content.isNotBlank()) {
            val userMessage = SolanaVS.ChatMessage(
                content = content,
                isFromAI = false
            )
            
            val updatedMessages = currentState.chatMessages + userMessage
            _viewState.value = currentState.copy(
                chatMessages = updatedMessages,
                isHeaderExpanded = false
            )
            activity?.let {
                processUserMessage(content, activity)
            }
        }
    }

    private fun processUserMessage(content: String, activity: Activity) {
        viewModelScope.launch {
            try {
                // Show loading state
                val loadingMessage = SolanaVS.ChatMessage(
                    content = "Thinking...",
                    isFromAI = true,
                    messageType = SolanaVS.MessageType.TEXT
                )
                updateChatMessages(loadingMessage)
                val agent = SolanaAiAgent(
                    walletManager = walletManager as SolanaWalletManager,
                    activity = activity as FragmentActivity
                )
                agent.sendMessage(
                    content,
                    "", //TODO add history
                    onResponse = {
                        val aiResponse = SolanaVS.ChatMessage(
                            content = it,
                            isFromAI = true,
                            messageType = SolanaVS.MessageType.TEXT
                        )
                        updateChatMessages(aiResponse)
                    },
                    onError = {}
                )
            } catch (e: Exception) {
                handleError("Failed to process message: ${e.message}")
            }
        }
    }

    private fun updateChatMessages(newMessage: SolanaVS.ChatMessage) {
        val currentState = _viewState.value
        if (currentState is SolanaVS.Success) {
            _viewState.value = currentState.copy(
                chatMessages = currentState.chatMessages + newMessage
            )
        }
    }

    private fun handleError(errorMessage: String) {
        val currentState = _viewState.value
        if (currentState is SolanaVS.Success) {
            val errorChatMessage = SolanaVS.ChatMessage(
                content = errorMessage,
                isFromAI = true,
                messageType = SolanaVS.MessageType.ERROR
            )
            _viewState.value = currentState.copy(
                chatMessages = currentState.chatMessages + errorChatMessage
            )
        } else {
            _viewState.value = SolanaVS.Error(errorMessage)
        }
    }
}
