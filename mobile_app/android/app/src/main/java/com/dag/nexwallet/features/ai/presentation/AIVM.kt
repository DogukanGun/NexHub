package com.dag.nexwallet.features.ai.presentation

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.dag.nexwallet.R
import com.dag.nexwallet.base.BaseVM
import com.dag.nexwallet.base.components.bottomnav.BottomNavMessageManager
import com.dag.nexwallet.base.scroll.ScrollStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIVM @Inject constructor(
    private val scrollManager: ScrollStateManager,
    private val bottomNavManager: BottomNavMessageManager
) : BaseVM<AIVS>(AIVS.MakeDecision) {

    init {
        initPage()
    }

    fun initPage(){
        scrollManager.updateScrolling(true)
        viewModelScope.launch {
            bottomNavManager.showMessage("AI King")
        }
    }

    fun startCartoonFilterEvent(){
        _viewState.value = AIVS.StartCartoonFilter
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
}