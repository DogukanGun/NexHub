package com.dag.nexwallet.features.ai.presentation

import com.dag.nexwallet.base.BaseVS

sealed class AIVS: BaseVS {
    object Loading : AIVS()
    object MakeDecision: AIVS()
    object StartCartoonFilter: AIVS()
    object StartImageByTextCreation: AIVS()
    object StartVideoByTextCreation: AIVS()
}

