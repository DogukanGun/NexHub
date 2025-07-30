import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.dag.nexwallet.features.ai.presentation.AIVM
import com.dag.nexwallet.features.ai.presentation.AIVS
import com.dag.nexwallet.features.ai.presentation.DecisionMakerScreen
import com.dag.nexwallet.features.ai.presentation.TextToImageScreen
import com.dag.nexwallet.features.ai.presentation.ImagePopupDialog
import com.dag.nexwallet.ui.theme.gradientStart


@Composable
fun AIScreen(
    viewModal: AIVM = hiltViewModel()
){
    val state = viewModal.viewState.collectAsState()
    val context = LocalContext.current
    var showImagePopup by remember { mutableStateOf(false) }
    var currentImageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            when(state.value){
                AIVS.MakeDecision -> {
                    DecisionMakerScreen(
                        onVideoByTextCreation = {
                            viewModal.startVideoByTextCreationEvent()
                        },
                        onCreateImageByTextCreation = {
                            viewModal.startImageByTextCreationEvent()
                        }
                    )
                }
                AIVS.StartImageByTextCreation -> {
                    TextToImageScreen(
                        onBackClick = {
                            viewModal.startMakeDecisionEvent()
                        },
                        onGenerateClick = { message->
                            viewModal.generateImageByText(message)
                        },
                    )
                }
                is AIVS.ImageGenerationLoading -> {
                    // Show loading screen while generating image
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(64.dp),
                                color = gradientStart,
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            androidx.compose.material3.Text(
                                text = (state.value as AIVS.ImageGenerationLoading).message,
                                color = com.dag.nexwallet.ui.theme.primaryText
                            )
                        }
                    }
                }
                is AIVS.ImageGenerationResponse -> {
                    // Show the image popup when we get a response
                    currentImageBitmap = (state.value as AIVS.ImageGenerationResponse).image
                    showImagePopup = true
                    
                    // Go back to the text input screen
                    TextToImageScreen(
                        onBackClick = {
                            viewModal.startMakeDecisionEvent()
                        },
                        onGenerateClick = { message->
                            viewModal.generateImageByText(message)
                        },
                    )
                }

                else -> {}
            }
        }
    }
    
    // Show image popup if needed
    if (showImagePopup && currentImageBitmap != null) {
        ImagePopupDialog(
            imageBitmap = currentImageBitmap,
            onDismiss = {
                showImagePopup = false
                currentImageBitmap = null
                isDownloading = false
            },
            onDownload = {
                isDownloading = true
                viewModal.downloadImage(context, currentImageBitmap!!)
                // Reset downloading state after a delay
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1500) // 1.5 seconds delay
                    isDownloading = false
                }
            },
            isDownloading = isDownloading
        )
    }
}

@Composable
@Preview
fun AIScreenPreview(){
    AIScreen()
}