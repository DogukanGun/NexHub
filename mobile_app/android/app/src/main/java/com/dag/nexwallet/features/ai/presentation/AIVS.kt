package com.dag.nexwallet.features.ai.presentation

import android.graphics.Bitmap
import com.dag.nexwallet.base.BaseVS

sealed class AIVS: BaseVS {
    object Loading : AIVS()
    object MakeDecision: AIVS()
    object StartImageByTextCreation: AIVS()
    object StartVideoByTextCreation: AIVS()
    data class ImageGenerationResponse(val image: Bitmap): AIVS()
    data class ImageGenerationLoading(val message: String): AIVS()
    object ComingSoon: AIVS()
}

