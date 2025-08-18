package com.basit.aitattoomaker.data.repo

import android.graphics.Bitmap
import android.net.Uri
import com.basit.aitattoomaker.domain.Tattoo

interface TattooRepository {
    suspend fun getTattoos(): List<Tattoo>
    suspend fun getTattooById(id: String): Tattoo?
    // Add this new function
    suspend fun loadTattooBitmap(imageUrl: String): Bitmap
    suspend fun loadDefaultTattoo(): Bitmap
    suspend fun loadTattooFromUri(uri: Uri): Bitmap
}