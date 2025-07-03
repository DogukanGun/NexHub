import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dag.nexwallet.features.ai.presentation.AIVM
import com.dag.nexwallet.features.ai.presentation.AIVS
import com.dag.nexwallet.features.ai.presentation.ApplyCartoonFilterScreen
import com.dag.nexwallet.features.ai.presentation.CreateVideoScreen
import com.dag.nexwallet.features.ai.presentation.DecisionMakerScreen
import com.dag.nexwallet.features.ai.presentation.TextToImageScreen


@Composable
fun AIScreen(
    viewModal: AIVM = hiltViewModel()
){
    val state = viewModal.viewState.collectAsState()
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
                        onCartoonFilterSelected = {
                            viewModal.startCartoonFilterEvent()
                        },
                        onCreateImageByTextCreation = {
                            viewModal.startImageByTextCreationEvent()
                        }
                    )
                }
                AIVS.StartCartoonFilter -> {
                    ApplyCartoonFilterScreen(
                        onBackClick = {
                            viewModal.startMakeDecisionEvent()
                        },
                        onSelectImageClick = {},
                    )
                }
                AIVS.StartVideoByTextCreation -> {
                    CreateVideoScreen(
                        onBackClick = {
                            viewModal.startMakeDecisionEvent()
                        }
                    ) { }
                }
                AIVS.StartImageByTextCreation -> {
                    TextToImageScreen(
                        onBackClick = {
                            viewModal.startMakeDecisionEvent()
                        },
                        onGenerateClick = {  },
                    )
                }
                else -> {}
            }

        }

    }
}

@Composable
@Preview
fun AIScreenPreview(){
    AIScreen()
}