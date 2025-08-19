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
import androidx.core.graphics.scale
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.FragmentAitoolsBinding
import com.basit.aitattoomaker.presentation.ai_tools.adapter.TattooAdapter
import com.basit.aitattoomaker.presentation.ai_tools.model.Tattoo
import com.basit.aitattoomaker.presentation.utils.DialogUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
//import com.kaopiz.kprogresshud.KProgressHUD
import com.basit.library.stickerview.StickerFactory
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
    private lateinit var adapter: TattooAdapter
    var model:Int=1
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
//    private var imageSavingDialogue: KProgressHUD? = null
    private val pickStickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { loadSticker(it) }
    }
    val tattoo_list: ArrayList<Tattoo>?=arrayListOf(Tattoo("Dragon",R.drawable.dragon),Tattoo("Flower",R.drawable.flower),Tattoo("Fire",R.drawable.tattoo),Tattoo("Heart",R.drawable.heart))
    private var mActivity: FragmentActivity?=null
    // This property is only valid between onCreateView and
    // onDestroyView.
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity=requireActivity()
    }

    override fun onDetach() {
        super.onDetach()
        try {
            binding?.slStickerLayout?.removeAllSticker()
            scope.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        mActivity?.let { activity ->
            adapter = TattooAdapter { tattoo ->
                StickerFactory.currentSticker = StickerFactory.createSticker(context=activity, drawableId = tattoo.tattooId, alpha = 128)
                binding?.slStickerLayout?.addSticker(StickerFactory.currentSticker)
            }
            binding?.rvTattoo?.adapter = adapter
            binding?.rvTattoo?.setHasFixedSize(true)
            adapter.submitList(tattoo_list)
            DialogUtils.show(activity, "Processing...")
            binding?.btnLoadDefault?.setOnClickListener {
                if(binding?.rvTattoo?.visibility==View.VISIBLE){
                    binding?.rvTattoo?.visibility=View.GONE
                    binding?.opacityList?.visibility=View.GONE
                }else{
                    binding?.rvTattoo?.visibility=View.VISIBLE
                    binding?.opacityList?.visibility=View.GONE
                }
            }
            binding?.btnPickSticker?.setOnClickListener {
//                pickStickerLauncher.launch("image/*")
            }
            binding?.btnAlpha?.setOnClickListener {
                if(binding?.opacityList?.visibility==View.VISIBLE){
                    binding?.opacityList?.visibility=View.GONE
                    binding?.rvTattoo?.visibility= View.GONE
                }else{
                    binding?.opacityList?.visibility=View.VISIBLE
                    binding?.rvTattoo?.visibility= View.GONE
                }
//                pickStickerLauncher.launch("image/*")
            }
            binding?.opacity64?.setOnClickListener {
                binding?.slStickerLayout?.updateSticker(64)
            }
            binding?.opacity128?.setOnClickListener {
                binding?.slStickerLayout?.updateSticker(128)
            }
            binding?.opacity192?.setOnClickListener {
                binding?.slStickerLayout?.updateSticker(192)
            }
            binding?.opacity255?.setOnClickListener {
                binding?.slStickerLayout?.updateSticker(255)
            }
//            stickerAlpha=binding?.slStickerLayout?.stickers?.get(0)
            binding?.btnSave?.setOnClickListener { saveToGallery() }
            // Load something right away
            binding?.changePhoto?.setOnClickListener {
                DialogUtils.dialog?.show()
                if(model==4){
                    model=1
                }
                else{
                    model++
                }
                loadDefaultPhotoAndMask(model)
            }
        }
    }
    private fun loadDefaultPhotoAndMask(model:Int) {
        // Load default person photo from drawable
        val base = BitmapFactory.decodeResource(
            mActivity?.resources,
            if(model==1)
            R.drawable.model1 else if(model==2) R.drawable.model2 else if(model==3) R.drawable.model3 else R.drawable.model4
        ) ?: run {
            Toast.makeText(mActivity, "Failed to load default photo", Toast.LENGTH_SHORT).show()
            return
        }

        // Run segmentation on the base image
        runSegmentation(base) { (baseBitmap, maskBitmap, bgBitmap) ->
            if (maskBitmap == null || baseBitmap == null || bgBitmap == null) {
                Toast.makeText(mActivity, "Segmentation failed", Toast.LENGTH_SHORT).show()
                return@runSegmentation
            }
            // Apply image + mask to your custom view
            binding?.maskedStickerView?.apply {
                setImageAndMask(baseBitmap, maskBitmap)
                DialogUtils.dialog?.dismiss()
            }
            binding?.bgImage?.apply {
                setImageAndMask(bgBitmap, bgBitmap)
                DialogUtils.dialog?.dismiss()
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
    private fun saveToGallery() {
        binding?.slStickerLayout?.clearFocusAll()
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
        binding?.slStickerLayout?.removeAllSticker()
        scope.cancel()
    }
}
/** tiny suspend helper to await Task<T> */
private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it, null) }
        addOnFailureListener { e -> cont.resumeWith(Result.failure(e)) }
        addOnCanceledListener { cont.cancel() }
    }