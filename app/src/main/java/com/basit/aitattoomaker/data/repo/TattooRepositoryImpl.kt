package com.basit.aitattoomaker.data.repo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.basit.aitattoomaker.domain.Tattoo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class TattooRepositoryImpl(private val context: Context) : TattooRepository {

    override suspend fun getTattoos(): List<Tattoo> {
        // This should return the same list structure you originally had
        return listOf(
            Tattoo(
                id = "1",
                name = "Dragon",
                imageUrl = "drawable/dragon.png",
                thumbnailUrl = "drawable/dragon.png",
                category = "Animals"
            ),
            // Add more tattoos as needed
        )
    }

    override suspend fun getTattooById(id: String): Tattoo? {
        return getTattoos().firstOrNull { it.id == id }
    }
    // Add to TattooRepository/TattooRepositoryImpl
    override suspend fun loadDefaultTattoo(): Bitmap {
        // Load from assets or resources
        val inputStream = context.assets.open("default_tattoo.png")
        return BitmapFactory.decodeStream(inputStream)
    }

    override suspend fun loadTattooFromUri(uri: Uri): Bitmap {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: throw IOException("Failed to decode image")
        }
    }
    // New function to load bitmap from assets
    override suspend fun loadTattooBitmap(imageUrl: String): Bitmap {
        return withContext(Dispatchers.IO) {
            try {
                val assetPath = imageUrl.replace("assets://", "")
                val inputStream = context.assets.open(assetPath)
                BitmapFactory.decodeStream(inputStream) ?: throw IOException("Bitmap decode failed")
            } catch (e: Exception) {
                throw IOException("Failed to load tattoo bitmap from $imageUrl", e)
            }
        }
    }
}