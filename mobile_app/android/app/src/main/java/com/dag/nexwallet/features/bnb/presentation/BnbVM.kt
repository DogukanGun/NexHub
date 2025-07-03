package com.dag.nexwallet.features.bnb.presentation

import com.dag.nexwallet.base.BaseVM
import com.dag.nexwallet.base.components.bottomnav.BottomNavMessageManager
import com.dag.nexwallet.base.scroll.ScrollStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BnbVM @Inject constructor(
    scrollManager: ScrollStateManager,
    private val bottomNavManager: BottomNavMessageManager
) : BaseVM<BnbVS>(BnbVS.Loading) {

}