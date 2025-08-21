package com.basit.aitattoomaker.presentation.ai_tools

import android.content.ContentValues
import android.content.Context
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.scale
import androidx.core.view.isVisible
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.FragmentAitoolsBinding
import com.basit.aitattoomaker.presentation.ai_tools.adapter.TattooAdapter
import com.basit.aitattoomaker.presentation.ai_tools.model.Tattoo
import com.basit.aitattoomaker.presentation.utils.DialogUtils
import com.basit.aitattoomaker.presentation.utils.DialogUtils.dialog
import com.basit.library.stickerview.StickerFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.nio.FloatBuffer

class AiToolsFragment : Fragment() {

    private var binding: FragmentAitoolsBinding? = null

    private lateinit var adapter: TattooAdapter
    private var modelIndex = 0

    // Single, reusable ML Kit options
    private val selfieOptions by lazy {
        SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .build()
    }

    // If you want custom sticker pick (kept wired but not triggered)
    private val pickStickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { loadSticker(it) } }

    private val tattooItems = listOf(
        Tattoo("Dragon", R.drawable.dragon),
        Tattoo("Flower", R.drawable.flower),
        Tattoo("Fire",   R.drawable.tattoo),
        Tattoo("Heart",  R.drawable.heart)
    )
    private var mActivity: FragmentActivity?=null
    // This property is only valid between onCreateView and
    // onDestroyView.
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity=requireActivity()
    }

    override fun onDetach() {
        super.onDetach()
        mActivity=null
    }
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
        mActivity?.let {
            try {
                setupRecycler()
                setupClicks()
                // Initial load
                DialogUtils.show(it, "Processing...")
                dialog?.show()
                cycleAndLoadModel() // loads model1 initially
            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }

    }

    // ---- UI setup ----

    private fun setupRecycler(){
        binding?.apply {

            adapter = TattooAdapter { tattoo ->
                // Add sticker with default alpha 128
                StickerFactory.currentSticker =
                    StickerFactory.createSticker(context = requireContext(), drawableId = tattoo.tattooId, alpha = 128)
                slStickerLayout.addSticker(StickerFactory.currentSticker)
            }
            rvTattoo.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rvTattoo.setHasFixedSize(true)
            rvTattoo.adapter = adapter
            adapter.submitList(tattooItems)
        }

    }

    private fun setupClicks(){
        binding?.apply {
            btnLoadDefault.setOnClickListener {
                // Toggle list panel(s)
                rvTattoo.isVisible = !rvTattoo.isVisible
                opacityList.isVisible = false
            }

            btnPickSticker.setOnClickListener {
                // pickStickerLauncher.launch("image/*")
            }

            btnAlpha.setOnClickListener {
                opacityList.isVisible = !opacityList.isVisible
                rvTattoo.isVisible = false
            }

            opacity64.setOnClickListener { slStickerLayout.updateSticker(64) }
            opacity128.setOnClickListener { slStickerLayout.updateSticker(128) }
            opacity192.setOnClickListener { slStickerLayout.updateSticker(192) }
            opacity255.setOnClickListener { slStickerLayout.updateSticker(255) }

            btnSave.setOnClickListener { saveToGallery() }

            changePhoto.setOnClickListener {
                dialog?.show()
                cycleAndLoadModel()
            }
        }

    }

    private fun cycleAndLoadModel() {
        // 4 demo models cycling [1..4]
        modelIndex = (modelIndex % 4) + 1
        loadDefaultPhotoAndMask(modelIndex)
    }

    private fun applyContainerRatio(photoW: Int, photoH: Int) {
        val params = binding?.photoContainer?.layoutParams as ConstraintLayout.LayoutParams
        params.dimensionRatio = "H,$photoW:$photoH"
        binding?.photoContainer?.layoutParams = params
    }

    // ---- Image load + segmentation ----

    private fun loadDefaultPhotoAndMask(model: Int) {
        val resId = when (model) {
            1 -> R.drawable.model1
            2 -> R.drawable.model2
            3 -> R.drawable.model3
            else -> R.drawable.model4
        }

        val base = BitmapFactory.decodeResource(resources, resId)
        if (base == null) {
            Toast.makeText(requireContext(), "Failed to load default photo", Toast.LENGTH_SHORT).show()
            dialog?.dismiss()
            return
        }

        runSegmentation(base) { baseBitmap, maskBitmap, bgBitmap ->
            if (baseBitmap == null || maskBitmap == null || bgBitmap == null) {
                Toast.makeText(requireContext(), "Segmentation failed", Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
                return@runSegmentation
            }

            applyContainerRatio(base.width, base.height)

            binding?.maskedStickerView?.setImageAndMask(baseBitmap, maskBitmap)
            binding?.bgImage?.setImageAndMask(bgBitmap, bgBitmap)

            dialog?.dismiss()
        }
    }

    private fun runSegmentation(
        base: Bitmap,
        onDone: (base: Bitmap?, mask: Bitmap?, bg: Bitmap?) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            try {
                val segmenter = Segmentation.getClient(selfieOptions)
                val result = withContext(Dispatchers.Main) {
                    segmenter.process(InputImage.fromBitmap(base, 0)).await()
                }

                val maskBuffer = result.buffer
                val maskW = result.width
                val maskH = result.height

                maskBuffer.rewind()
                val floatBuffer = maskBuffer.asFloatBuffer()

                // 1) Build mask bitmap from probabilities, scale to base size
                val maskBitmap = floatsToMaskBitmap(floatBuffer, maskW, maskH)
                    .scale(base.width, base.height, filter = true)

                // 2) Build background with person cut out (DST_OUT)
                val bgBitmap = Bitmap.createBitmap(base.width, base.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bgBitmap)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                }
                canvas.drawBitmap(base, 0f, 0f, paint)
                canvas.drawBitmap(maskBitmap, 0f, 0f, maskPaint)

                withContext(Dispatchers.Main) { onDone(base, maskBitmap, bgBitmap) }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { onDone(null, null, null) }
            }
        }
    }

    /**
     * Convert ML Kit segmentation FloatBuffer → ARGB mask (white for person, alpha = confidence)
     */
    private fun floatsToMaskBitmap(buffer: FloatBuffer, maskW: Int, maskH: Int): Bitmap {
        val pixels = IntArray(maskW * maskH)
        for (y in 0 until maskH) {
            for (x in 0 until maskW) {
                val idx = y * maskW + x
                val confidence = buffer.get(idx) // 0..1
                val alpha = (confidence * 255).toInt().coerceIn(0, 255)
                pixels[idx] = Color.argb(alpha, 255, 255, 255)
            }
        }
        return Bitmap.createBitmap(pixels, maskW, maskH, Bitmap.Config.ARGB_8888)
    }

    private fun loadSticker(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val bm = requireContext().contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            withContext(Dispatchers.Main) {
                if (bm != null) {
                    binding?.maskedStickerView?.setSticker(bm)
                } else {
                    Toast.makeText(requireContext(), "Sticker load failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ---- Save composited result ----

    private fun saveToGallery() {
        // Ensure current edits applied visually
        binding?.slStickerLayout?.clearFocusAll()
        mActivity?.let {
            DialogUtils.show(it, "Saving...")
        }
        dialog?.show()
        val outBitmap = binding?.photoContainer?.drawToBitmap() // composed output
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            outBitmap?.let {
                val ok = saveBitmapToGallery(outBitmap, "tattoo_result_${System.currentTimeMillis()}.png")
                withContext(Dispatchers.Main) {
                    dialog?.dismiss()
                    Toast.makeText(requireContext(), if (ok) "Saved to gallery" else "Save failed", Toast.LENGTH_SHORT).show()
                }
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

        val cv = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(imageCollection, cv) ?: return false
        var os: OutputStream? = null
        return try {
            os = resolver.openOutputStream(uri)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os ?: return false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cv.clear()
                cv.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, cv, null, null)
            }
            true
        } catch (_: Exception) {
            false
        } finally {
            os?.close()
        }
    }

    // ---- Lifecycle ----

    override fun onDestroyView() {
        // Clean stickers before dropping binding to avoid NPE
        try {
            binding?.slStickerLayout?.removeAllSticker()
        } catch (_: Exception) {}
        super.onDestroyView()
    }
}

/** Await Task<T> without leaking */
private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it, null) }
        addOnFailureListener { e -> cont.resumeWith(Result.failure(e)) }
        addOnCanceledListener { cont.cancel() }
    }
