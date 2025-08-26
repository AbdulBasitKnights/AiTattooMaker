package com.basit.aitattoomaker.presentation.camera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.media.Image
import android.net.Uri
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.basit.aitattoomaker.data.repo.TattooRepository
import com.basit.aitattoomaker.domain.Tattoo
import com.basit.aitattoomaker.presentation.ai_tools.model.CameraTattoo
import com.basit.aitattoomaker.presentation.utils.CameraPermissionHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalGetImage
class CameraViewModel(
    application: Application,
    private val tattooRepository: TattooRepository
) : AndroidViewModel(application) {

    private val _segmentationResult = MutableLiveData<Bitmap>()
    val segmentationResult: LiveData<Bitmap> = _segmentationResult

    private val _selectedTattoo = MutableLiveData<Bitmap?>()
    val selectedTattoo: LiveData<Bitmap?> = _selectedTattoo

    private val _tattoos = MutableLiveData<List<CameraTattoo>>()
    val tattoos: LiveData<List<CameraTattoo>> = _tattoos

    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> = _loadingState

    private val _errorState = MutableLiveData<String?>()
    val errorState: LiveData<String?> = _errorState

    private val _permissionGranted = MutableLiveData<Boolean>()
    val permissionGranted: LiveData<Boolean> = _permissionGranted

    private val _capturedImage = MutableLiveData<Bitmap?>()
    val capturedImage: LiveData<Bitmap?> = _capturedImage

    private val _finalResult = MutableLiveData<Bitmap?>()
    val finalResult: LiveData<Bitmap?> = _finalResult

    private var latestFrame: Bitmap? = null
    private var latestMask: Bitmap? = null

    private val segmenter by lazy {
        val options = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)
            .build()
        Segmentation.getClient(options)
    }

    init {
        loadTattoos()
    }
    fun loadDefaultTattoo(): LiveData<Bitmap?> {
        val result = MutableLiveData<Bitmap?>()
        viewModelScope.launch {
            try {
                val bitmap = tattooRepository.loadDefaultTattoo()
                result.postValue(bitmap)
            } catch (e: Exception) {
                _errorState.postValue("Failed to load default tattoo")
            }
        }
        return result
    }

    fun loadTattooFromUri(uri: Uri): LiveData<Bitmap?> {
        val result = MutableLiveData<Bitmap?>()
        viewModelScope.launch {
            try {
                val bitmap = tattooRepository.loadTattooFromUri(uri)
                result.postValue(bitmap)
            } catch (e: Exception) {
                _errorState.postValue("Failed to load tattoo")
            }
        }
        return result
    }
    // Frame capture and tattoo application
/*    suspend fun captureCurrentFrame(): Bitmap? {
        return latestFrame?.let { frameBitmap ->
            latestMask?.let { maskBitmap ->
                _selectedTattoo.value?.let { tattooBitmap ->
                    val tattooPosition = calculateTattooPosition(frameBitmap, tattooBitmap)
                    applyTattooOnBody(frameBitmap, maskBitmap, tattooBitmap, tattooPosition)
                }
            }
        }
    }*/
    fun captureCurrentFrame(tattooPosition: RectF): LiveData<CaptureResult> {
        val result = MutableLiveData<CaptureResult>()
        viewModelScope.launch {
            try {
                // Verify all required components
                val frame = latestFrame ?: throw CaptureException("No camera frame available")
                val mask = latestMask ?: throw CaptureException("Body not properly detected")
                val tattoo = _selectedTattoo.value ?: throw CaptureException("No tattoo selected")

                // Process in background
                val resultBitmap = withContext(Dispatchers.Default) {
                    val mutableBitmap = frame.copy(Bitmap.Config.ARGB_8888, true)
                    val canvas = Canvas(mutableBitmap)

                    val paint = Paint().apply {
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
                        isAntiAlias = true
                    }

                    canvas.drawBitmap(tattoo, null, tattooPosition, paint)
                    mutableBitmap
                }

                result.postValue(CaptureResult.Success(resultBitmap))
            } catch (e: CaptureException) {
                result.postValue(CaptureResult.Error(e.message ?: "Capture failed"))
            } catch (e: Exception) {
                result.postValue(CaptureResult.Error("Processing error: ${e.message}"))
            }
        }
        return result
    }

    sealed class CaptureResult {
        data class Success(val bitmap: Bitmap) : CaptureResult()
        data class Error(val message: String) : CaptureResult()
    }

    class CaptureException(message: String) : Exception(message)
    private fun calculateTattooPosition(frameBitmap: Bitmap, tattooBitmap: Bitmap): RectF {
        // Default position - center of the frame
        return RectF(
            frameBitmap.width / 2f - tattooBitmap.width / 2f,
            frameBitmap.height / 2f - tattooBitmap.height / 2f,
            frameBitmap.width / 2f + tattooBitmap.width / 2f,
            frameBitmap.height / 2f + tattooBitmap.height / 2f
        )
    }

    private suspend fun applyTattooOnBody(
        frameBitmap: Bitmap,
        maskBitmap: Bitmap,
        tattooBitmap: Bitmap,
        tattooPosition: RectF
    ): Bitmap = withContext(Dispatchers.Default) {
        val resultBitmap = frameBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)

        val paint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
            isAntiAlias = true
        }

        canvas.drawBitmap(
            tattooBitmap,
            null,
            tattooPosition,
            paint
        )

        return@withContext resultBitmap
    }

    // Image analysis and processing
    val analyzer = ImageAnalysis.Analyzer { imageProxy ->
        try {
            val mediaImage = imageProxy.image ?: return@Analyzer

            // Store current frame
            latestFrame = imageProxy.toBitmap(mediaImage)

            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            segmenter.process(image)
                .addOnSuccessListener { segmentationMask ->
                    latestMask = processSegmentationMask(segmentationMask)
                    latestMask?.let { _segmentationResult.postValue(it) }
                }
                .addOnFailureListener { e ->
                    _errorState.postValue("Segmentation failed: ${e.message}")
                    Log.e("CameraViewModel", "Segmentation failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } catch (e: Exception) {
            _errorState.postValue("Image processing error: ${e.message}")
            Log.e("CameraViewModel", "Image analysis failed", e)
            imageProxy.close()
        }
    }

    private fun processSegmentationMask(segmentationMask: SegmentationMask): Bitmap {
        val maskBitmap = Bitmap.createBitmap(
            segmentationMask.width,
            segmentationMask.height,
            Bitmap.Config.ARGB_8888
        )

        val buffer = segmentationMask.buffer
        val pixels = IntArray(segmentationMask.width * segmentationMask.height)

        for (i in pixels.indices) {
            val confidence = buffer.float
            pixels[i] = if (confidence > 0.5f) {
                Color.argb((confidence * 255).toInt(), 255, 255, 255)
            } else {
                Color.TRANSPARENT
            }
        }

        maskBitmap.setPixels(
            pixels,
            0,
            segmentationMask.width,
            0,
            0,
            segmentationMask.width,
            segmentationMask.height
        )

        return maskBitmap
    }

    // Extension function to convert ImageProxy to Bitmap
    private fun ImageProxy.toBitmap(mediaImage: Image): Bitmap {
        val buffer = mediaImage.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        // Correct orientation
        val rotationDegrees = this.imageInfo.rotationDegrees
        return if (rotationDegrees != 0) {
            val matrix = Matrix().apply {
                postRotate(rotationDegrees.toFloat())
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    // Tattoo selection and loading
    fun selectTattoo(tattoo: CameraTattoo) {
        viewModelScope.launch {
            _loadingState.postValue(true)
            try {
                val bitmap = tattooRepository.loadTattooBitmap(tattoo.imageUrl)
                _selectedTattoo.postValue(bitmap)
                _errorState.postValue(null)
            } catch (e: Exception) {
                _errorState.postValue("Failed to load tattoo: ${e.message}")
                Log.e("CameraViewModel", "Error loading tattoo", e)
            } finally {
                _loadingState.postValue(false)
            }
        }
    }

    fun clearSelection() {
        _selectedTattoo.postValue(null)
    }

    // Permission handling
    fun checkCameraPermission() {
        _permissionGranted.value = CameraPermissionHelper.hasCameraPermission(getApplication())
    }

    // Tattoo list loading
    private fun loadTattoos() {
        viewModelScope.launch {
            _loadingState.postValue(true)
            try {
//                _tattoos.postValue(tattooRepository.getTattoos())
                _errorState.postValue(null)
            } catch (e: Exception) {
                _errorState.postValue("Failed to load tattoos: ${e.message}")
                Log.e("CameraViewModel", "Error loading tattoos", e)
            } finally {
                _loadingState.postValue(false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            segmenter.close()
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Error closing segmenter", e)
        }
    }
}