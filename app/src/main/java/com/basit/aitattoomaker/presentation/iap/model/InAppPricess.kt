package com.basit.aitattoomaker.presentation.iap.model

import androidx.annotation.Keep

@Keep
data class InAppPricess(
    var name: String = "",
    var price: String = "",
    var key: String = "",
    var freeTrial: String = ""
)