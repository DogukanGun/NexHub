package com.dag.nexwallet.features.ai.presentation

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.dag.nexwallet.R
import com.dag.nexwallet.base.BaseVM
import com.dag.nexwallet.base.components.bottomnav.BottomNavMessageManager
import com.dag.nexwallet.base.scroll.ScrollStateManager
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.asImageOrNull
import com.google.firebase.ai.type.generationConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIVM @Inject constructor(
    private val scrollManager: ScrollStateManager,
    private val bottomNavManager: BottomNavMessageManager
) : BaseVM<AIVS>(AIVS.MakeDecision) {

    val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
        modelName = "gemini-2.0-flash-preview-image-generation",
        generationConfig = generationConfig {
            responseModalities = listOf(ResponseModality.TEXT, ResponseModality.IMAGE)
        }
    )

    init {
        initPage()
    }

    fun initPage(){
        scrollManager.updateScrolling(true)
        viewModelScope.launch {
            bottomNavManager.showMessage("AI Hub")
        }
    }

    fun startImageByTextCreationEvent(){
        _viewState.value = AIVS.StartImageByTextCreation
    }

    fun startVideoByTextCreationEvent(){
        _viewState.value = AIVS.StartVideoByTextCreation
    }

    fun startMakeDecisionEvent() {
        _viewState.value = AIVS.MakeDecision
    }

    fun generateVideoByText(message: String) {
        viewModelScope.launch {

        }
    }

    fun generateImageByText(message: String) {
        viewModelScope.launch {
            val generatedImageAsBitmap = model.generateContent(message)
                .candidates.first().content.parts.firstNotNullOf { it.asImageOrNull() }
            _viewState.value = AIVS.ImageGenerationResponse(generatedImageAsBitmap)
        }
    }
}