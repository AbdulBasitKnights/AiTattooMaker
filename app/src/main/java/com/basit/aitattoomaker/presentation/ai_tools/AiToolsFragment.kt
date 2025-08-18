package com.basit.aitattoomaker.presentation.ai_tools

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.scale
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.FragmentAitoolsBinding
import com.basit.aitattoomaker.presentation.utils.DialogUtils
import com.bumptech.glide.Glide
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
//import com.kaopiz.kprogresshud.KProgressHUD
import com.lcw.library.stickerview.Sticker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.nio.FloatBuffer

class AiToolsFragment : Fragment() {

    private var binding: FragmentAitoolsBinding? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
//    private var imageSavingDialogue: KProgressHUD? = null
    private val pickStickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { loadSticker(it) }
    }
    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAitoolsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        DialogUtils.show(requireContext(), "Saving...")
        binding?.btnLoadDefault?.setOnClickListener {
            val stickers = Sticker(
                requireContext(),
                BitmapFactory.decodeResource(resources, R.drawable.tattoo)
            )

            binding?.slStickerLayout?.addSticker(stickers)

        }
        binding?.btnPickSticker?.setOnClickListener { pickStickerLauncher.launch("image/*") }
        binding?.btnSave?.setOnClickListener { saveToGallery() }

        // Load something right away
        loadDefaultPhotoAndMask()
        // Set a default sticker (png in drawable)

    }
    private fun loadDefaultPhotoAndMask() {
        // Load default person photo from drawable
        val base = BitmapFactory.decodeResource(
            requireActivity().resources,
            R.drawable.boy1
        ) ?: run {
            Toast.makeText(requireContext(), "Failed to load default photo", Toast.LENGTH_SHORT).show()
            return
        }

        // Run segmentation on the base image
        runSegmentation(base) { (baseBitmap, maskBitmap, bgBitmap) ->
            if (maskBitmap == null || baseBitmap == null || bgBitmap == null) {
                Toast.makeText(requireContext(), "Segmentation failed", Toast.LENGTH_SHORT).show()
                return@runSegmentation
            }

            // Apply image + mask to your custom view
            binding?.maskedStickerView?.apply {
                setImageAndMask(baseBitmap, maskBitmap)

                // Load tattoo sticker PNG
//                val sticker = BitmapFactory.decodeResource(
//                    requireActivity().resources,
//                    R.drawable.tattoo
//                )
//                setSticker(sticker)
            }
            binding?.bgImage?.apply {
                setImageAndMask(bgBitmap, bgBitmap)
            }

            // (Optional) Preview background without person
            // Glide.with(requireContext())
            //     .load(bgBitmap)
            //     .into(binding?.backgroundImage!!)
        }
    }


    private fun runSegmentation(base: Bitmap, onDone: (Triple<Bitmap?, Bitmap?, Bitmap?>) -> Unit) {
        scope.launch(Dispatchers.Default) {
            try {
                val options = SelfieSegmenterOptions.Builder()
                    .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                    .build()

                val segmenter = Segmentation.getClient(options)
                val image = InputImage.fromBitmap(base, 0)

                val result = withContext(Dispatchers.Main) {
                    segmenter.process(image).await()
                }

                val maskBuffer = result.buffer
                val maskW = result.width
                val maskH = result.height

                // convert ByteBuffer → FloatBuffer
                maskBuffer.rewind()
                val floatBuffer = maskBuffer.asFloatBuffer()

                // 1) mask bitmap
                val maskBitmap = floatsToMaskBitmap(floatBuffer, maskW, maskH)
                    .scale(base.width, base.height, filter = true)

                // 2) background without person (transparent where mask is person)
                val bgBitmap = Bitmap.createBitmap(base.width, base.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bgBitmap)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)

                // use mask as alpha to cut out the person
                val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                }

                // draw original image
                canvas.drawBitmap(base, 0f, 0f, paint)
                // punch out mask (transparent where person was detected)
                canvas.drawBitmap(maskBitmap, 0f, 0f, maskPaint)

                withContext(Dispatchers.Main) {
                    onDone(Triple(base, maskBitmap, bgBitmap))
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onDone(Triple(null, null, null)) }
            }
        }
    }

    /**
     * Convert ML Kit segmentation FloatBuffer to ARGB Bitmap (white for person, transparent outside)
     */
    private fun floatsToMaskBitmap(buffer: FloatBuffer, maskW: Int, maskH: Int): Bitmap {
        val pixels = IntArray(maskW * maskH)
        for (y in 0 until maskH) {
            for (x in 0 until maskW) {
                val idx = y * maskW + x
                val confidence = buffer.get(idx) // [0..1] person probability
                val alpha = (confidence * 255).toInt().coerceIn(0, 255)
                pixels[idx] = Color.argb(alpha, 255, 255, 255) // white for person
            }
        }
        return Bitmap.createBitmap(pixels, maskW, maskH, Bitmap.Config.ARGB_8888)
    }




    private fun loadSticker(uri: Uri) {
        scope.launch(Dispatchers.IO) {
            val bm = requireContext().contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            }
            withContext(Dispatchers.Main) {
                if (bm != null) binding?.maskedStickerView?.setSticker(bm)
                else Toast.makeText(requireContext(), "Sticker load failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun View.toBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }
    private fun saveToGallery() {
        binding?.slStickerLayout?.clearFocus()
        DialogUtils.dialog?.show()
        val out = binding?.photoContainer?.drawToBitmap()
        if (out == null) {
            Toast.makeText(requireContext(), "Nothing to save", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch(Dispatchers.IO) {
            val saved = saveBitmapToGallery(out, "tattoo_result_${System.currentTimeMillis()}.png")
            withContext(Dispatchers.Main) {
                DialogUtils.dialog?.dismiss()
                Toast.makeText(requireContext(), if (saved) "Saved to gallery" else "Save failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap, fileName: String): Boolean {
        val resolver = requireContext().contentResolver
        val imageCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(imageCollection, contentValues) ?: return false
        var out: OutputStream? = null
        return try {
            out = resolver.openOutputStream(uri)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out?: return false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            true
        } catch (_: Exception) {
            false
        } finally {
            out?.close()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        scope.cancel()
    }
}

/** Convert ML Kit SegmentationMask buffer (FloatBuffer, person probs) into ARGB mask bitmap. */
//private fun floatsToMaskBitmap(buffer: java.nio.ByteBuffer, width: Int, height: Int): Bitmap {
//    buffer.rewind()
//    val fb = buffer.asFloatBuffer()
//    val pixels = IntArray(width * height)
//    val tmp = FloatArray(width * height)
//    fb.get(tmp)
//
//    for (i in tmp.indices) {
//        val p = (tmp[i] * 255f).toInt().coerceIn(0, 255) // person prob -> alpha
//        // White with alpha = person probability
//        pixels[i] = Color.argb(p, 255, 255, 255)
//    }
//    return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
//}

/** tiny suspend helper to await Task<T> */
private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it, null) }
        addOnFailureListener { e -> cont.resumeWith(Result.failure(e)) }
        addOnCanceledListener { cont.cancel() }
    }