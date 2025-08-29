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
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.FragmentAitoolsBinding
import com.basit.aitattoomaker.extension.setDrawableTint
import com.basit.aitattoomaker.extension.show
import com.basit.aitattoomaker.extension.showDiscardDialog
import com.basit.aitattoomaker.extension.showDownloadDialog
import com.basit.aitattoomaker.extension.uriToBitmap
import com.basit.aitattoomaker.presentation.ai_tools.model.CameraTattoo
import com.basit.aitattoomaker.presentation.camera.adapter.CameraTattooAdapter
import com.basit.aitattoomaker.presentation.utils.AppUtils.tattooPath
import com.basit.aitattoomaker.presentation.utils.DialogUtils
import com.basit.aitattoomaker.presentation.utils.DialogUtils.dialog
import com.basit.aitattoomaker.presentation.utils.capturedBitmap
import com.basit.aitattoomaker.presentation.utils.tattooCreation
import com.basit.library.stickerview.Sticker
import com.basit.library.stickerview.StickerFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.nio.FloatBuffer

class AiToolsFragment : Fragment() {

    private var binding: FragmentAitoolsBinding? = null
    private val tattooViewModel: AiToolsViewModel by viewModels()
    private lateinit var adapter: CameraTattooAdapter
    private var maskedBitmap: Bitmap?=null
    private var modelIndex = 0
    // If you want custom sticker pick (kept wired but not triggered)
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                mActivity?.uriToBitmap(uri)?.let {
                    capturedBitmap=it
                    cycleAndLoadModel(true)
                }
            } else {
                dialog?.dismiss()
                Log.d("PhotoPicker", "No media selected")
            }
        }

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
                tattooCreation.postValue(false)
                setupRecycler()
                setupClicks()
                // Initial load
                DialogUtils.show(it, "Processing...")
                dialog?.show()
                cycleAndLoadModel(first = true) // loads model1 initially
            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }

    }

    fun addStickerToLayout(sticker: Sticker?) {
        sticker?.let {
            binding?.slStickerLayout?.addSticker(it)
        }
    }

    // ---- UI setup ----

    private fun setupRecycler(){

        binding?.apply {

            adapter = CameraTattooAdapter { tattoo ->
                // Add sticker with default alpha 128
                StickerFactory.currentSticker = StickerFactory.createStickerFromAsset(
                    context = requireContext(),
                    assetPath = tattoo.imageUrl,  // can be "library/dragon.png" OR "file:///android_asset/library/dragon.png"
                    alpha = 128
                )
                StickerFactory.currentSticker?.let {sticker->
                    maskedBitmap?.let {
                        addStickerToLayout(sticker)
                    }
                }
            }
            rvTattoo.adapter = adapter
            adapter.submitList(tattooViewModel.library.value?.toList())
        }

    }

    private fun setupClicks(){
        binding?.apply {
            mActivity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
                mActivity?.showDiscardDialog(
                    onDiscard = {
                        // User clicked discard, handle accordingly
                        findNavController().popBackStack() // example: go back
                    },
                    onNotNow = {
                        // User clicked not now, just dismiss dialog
                    }
                )
            }
            tabLibrary.setOnClickListener {
                tabLibrary.setTextColor(resources.getColor(R.color.colorprimary))
                tabHistory.setTextColor(resources.getColor(R.color.disable))
                rvTattoo.invalidate()
                adapter.submitList(tattooViewModel.library.value?.toList())
                tabLibrary.setDrawableTint(resources.getColor(R.color.colorprimary))
                tabHistory.setDrawableTint(resources.getColor(R.color.disable))
            }
            tabHistory.setOnClickListener {
                tabLibrary.setTextColor(resources.getColor(R.color.disable))
                tabHistory.setTextColor(resources.getColor(R.color.colorprimary))
                rvTattoo.invalidate()
                adapter.submitList(tattooViewModel.history.value?.toList())
                tabHistory.setDrawableTint(resources.getColor(R.color.colorprimary))
                tabLibrary.setDrawableTint(resources.getColor(R.color.disable))
            }
            binding?.cancelOpacity?.setOnClickListener {
                slStickerLayout.updateSticker(128)
                binding?.slStickerLayout?.clearFocusAll()
            }
            binding?.doneOpacity?.setOnClickListener {
                binding?.slStickerLayout?.clearFocusAll()
            }
            binding?.cross?.setOnClickListener {
                try {
                    mActivity?.showDiscardDialog(
                        onDiscard = {
                            // User clicked discard, handle accordingly
                            findNavController().popBackStack() // example: go back
                        },
                        onNotNow = {
                            // User clicked not now, just dismiss dialog
                        }
                    )
                }
                catch (e:Exception){
                    e.printStackTrace()
                }
            }
            opacitySeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val steps = listOf(0,20,40,60,80,100)
                        val nearest = steps.minByOrNull { kotlin.math.abs(it - progress) } ?: 0
                        seekBar?.progress = nearest
                        binding?.seekbarValue?.text=nearest.toString()
                        when (nearest){
                            0 -> slStickerLayout.updateSticker(20)
                            20 -> slStickerLayout.updateSticker(64)
                            40 -> slStickerLayout.updateSticker(128)
                            60 -> slStickerLayout.updateSticker(162)
                            80 -> slStickerLayout.updateSticker(195)
                            else -> slStickerLayout.updateSticker(215)
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            StickerFactory?.isStickerFocused?.observe(viewLifecycleOwner){
                if(it==true){
                    removeSticker.show()
                    opacityLayout.show()
                    bottom.visibility = View.INVISIBLE
                    save.visibility= View.INVISIBLE
                }
                else{
                    instructionOverlay.visibility= View.INVISIBLE
                    removeSticker.visibility=View.INVISIBLE
                    opacityLayout.visibility=View.INVISIBLE
                    bottom.show()
                    save.visibility= View.VISIBLE
                    rvTattoo.invalidate()
                }
            }
            removeSticker.setOnClickListener {
                StickerFactory?.currentSticker?.let {
                    binding?.slStickerLayout?.removeSticker(it)
                    binding?.slStickerLayout?.clearFocusAll()
                }
            }

            save.setOnClickListener { saveToGallery() }

            gallery.setOnClickListener {
                dialog?.show()
                openPicker()
            }
        }

    }
    fun openPicker() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun cycleAndLoadModel(first:Boolean=true) {
        // 4 demo models cycling [1..4]
        if(first){
            loadDefaultPhotoAndMask(modelIndex,true)
        }
        else{
            modelIndex = (modelIndex % 4) + 1
            loadDefaultPhotoAndMask(modelIndex,false)
        }

    }

    private fun applyContainerRatio(photoW: Int, photoH: Int) {
        val params = binding?.photoContainer?.layoutParams as ConstraintLayout.LayoutParams
        params.dimensionRatio = "H,$photoW:$photoH"
        binding?.photoContainer?.layoutParams = params
    }

    // ---- Image load + segmentation ----

    private fun loadDefaultPhotoAndMask(model: Int, first: Boolean = true) {
        StickerFactory.currentSticker = StickerFactory.createStickerFromAsset(
            context = requireContext(),
            assetPath = tattooPath,  // can be "library/dragon.png" OR "file:///android_asset/library/dragon.png"
            alpha = 128
        )
        val resId = when (model) {
            1 -> R.drawable.model1
            2 -> R.drawable.model2
            3 -> R.drawable.model3
            4 -> R.drawable.tattoowithbg
            else -> R.drawable.model4
        }

        val base = if (first) capturedBitmap else BitmapFactory.decodeResource(resources, resId)
        if (base == null) {
            Toast.makeText(requireContext(), "Failed to load default photo", Toast.LENGTH_SHORT).show()
            dialog?.dismiss()
            return
        }

        // run suspending segmentation safely
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            val (baseBitmap, maskBitmap, bgBitmap) = runSmartSegmentation(base)

            if (baseBitmap == null || maskBitmap == null) {
                Toast.makeText(requireContext(), "Segmentation failed", Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
                return@launch
            }

            applyContainerRatio(base.width, base.height)

            // ✅ Sticker view gets original image + mask
            binding?.maskedStickerView?.setImageAndMask(baseBitmap, maskBitmap)

            // ✅ Background view gets cutout background only
            bgBitmap?.let {
                binding?.bgImage?.setImageAndMask(bgBitmap, bgBitmap)
            }
            maskedBitmap=maskBitmap
            dialog?.dismiss()
            binding?.root?.show()
            StickerFactory.currentSticker?.let {sticker->
                    addStickerToLayout(sticker)
            }
        }
    }

    private suspend fun runSmartSegmentation(base: Bitmap): Triple<Bitmap?, Bitmap?, Bitmap?> {
        return try {
            val subjectSegmenter = SubjectSegmentation.getClient(
                SubjectSegmenterOptions.Builder()
                    .enableForegroundConfidenceMask() // ✅ correct place
                    .enableForegroundBitmap()
                    .build()
            )

            val result = subjectSegmenter.process(InputImage.fromBitmap(base, 0)).await()
            val subject = result.subjects.firstOrNull()

            if (subject?.confidenceMask != null) {
                var mask = floatsToMaskBitmap(subject.confidenceMask!!, subject.width, subject.height)
                    .scale(base.width, base.height, true)

                // 🔹 Compare mask coverage vs base area
                val maskPixels = IntArray(mask.width * mask.height)
                mask.getPixels(maskPixels, 0, mask.width, 0, 0, mask.width, mask.height)

                val nonZero = maskPixels.count { Color.alpha(it) > 10 } // pixels with visible alpha
                val total = maskPixels.size
                val coveragePercent = (nonZero.toFloat() / total.toFloat()) * 100f

                Log.d(
                    "Segmentation",
                    "Mask coverage = ${"%.2f".format(coveragePercent)}% of base (${nonZero} / ${total} pixels)"
                )

                // Optional: if mask coverage is too low, fallback to base as mask
                if (coveragePercent < 10f) {
                    Log.w("Segmentation", "⚠️ Mask too small (<10%), using base as mask")
                    Toast.makeText(mActivity, "Please Recapture the Image, subject are is too small", Toast.LENGTH_SHORT).show()
                    mask = base.copy(Bitmap.Config.ARGB_8888, true)
                }

                // 🔹 Build background cut-out
                val bg = Bitmap.createBitmap(base.width, base.height, Bitmap.Config.ARGB_8888).apply {
                    Canvas(this).apply {
                        drawBitmap(base, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG))
                        drawBitmap(mask, 0f, 0f, Paint().apply {
                            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                        })
                    }
                }

                Triple(base, mask, bg)
            } else {
                Log.e("Segmentation", "⚠️ Subject segmentation failed, falling back to Selfie")
                runSelfieSegmentation(base)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runSelfieSegmentation(base)
        }
    }


    private suspend fun runSelfieSegmentation(base: Bitmap): Triple<Bitmap?, Bitmap?, Bitmap?> {
        try {
            val selfieOptions = SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                .build()

            val segmenter = Segmentation.getClient(selfieOptions)
            val result = segmenter.process(InputImage.fromBitmap(base, 0)).await()

            val floatBuffer = result.buffer.asFloatBuffer()
            var mask = floatsToMaskBitmap(floatBuffer, result.width, result.height)
                .scale(base.width, base.height, true)

            // 🔹 Compare mask coverage vs base area
            val maskPixels = IntArray(mask.width * mask.height)
            mask.getPixels(maskPixels, 0, mask.width, 0, 0, mask.width, mask.height)

            val nonZero = maskPixels.count { Color.alpha(it) > 10 }
            val total = maskPixels.size
            val coveragePercent = (nonZero.toFloat() / total.toFloat()) * 100f

            Log.d(
                "SelfieSegmentation",
                "Mask coverage = ${"%.2f".format(coveragePercent)}% of base ($nonZero / $total pixels)"
            )

            // Optional: if mask coverage is too low (<10%), fallback to base
            if (coveragePercent < 10f) {
                Toast.makeText(mActivity, "Please Recapture the Image, subject are is too small", Toast.LENGTH_SHORT).show()

                Log.w("SelfieSegmentation", "⚠️ Mask too small (<10%), using base as mask")
                mask = base.copy(Bitmap.Config.ARGB_8888, true)
            }

            // 🔹 Build background cut-out
            val bg = createBitmap(base.width, base.height).apply {
                Canvas(this).apply {
                    drawBitmap(base, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG))
                    drawBitmap(mask, 0f, 0f, Paint().apply {
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                    })
                }
            }

            return Triple(base, mask, bg)
        } catch (e: Exception) {
            return Triple(null,null,null)
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
                val confidence = buffer.get(idx) // [0..1]
                val alpha = (confidence * 255).toInt().coerceIn(0, 255)
                // PURE alpha mask: subject opaque, background transparent
                pixels[idx] = Color.argb(alpha, 0, 0, 0)
            }
        }
        return Bitmap.createBitmap(pixels, maskW, maskH, Bitmap.Config.ARGB_8888)
    }


//    private fun loadSticker(uri: Uri) {
//        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
//            val bm = requireContext().contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
//            withContext(Dispatchers.Main) {
//                if (bm != null) {
//                    binding?.maskedStickerView?.setSticker(bm)
//                } else {
//                    Toast.makeText(requireContext(), "Sticker load failed", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }

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
                    if(ok){
                        mActivity?.showDownloadDialog()
                    }
                    else{
                        Toast.makeText(requireContext(), if (ok) "Saved to gallery" else "Save failed", Toast.LENGTH_SHORT).show()
                    }

                }
            }

        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap, fileName: String): Boolean {
        val resolver = requireContext().contentResolver
        val imageCollection =
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

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
