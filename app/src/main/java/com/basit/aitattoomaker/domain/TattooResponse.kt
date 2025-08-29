package com.basit.aitattoomaker.domain

import com.basit.aitattoomaker.presentation.ai_tools.model.CameraTattoo

data class TattooResponse(
    val library: List<CameraTattoo>,
    val history: List<CameraTattoo>
)