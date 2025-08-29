package com.basit.aitattoomaker.presentation.camera

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.data.repo.TattooRepositoryImpl
import com.basit.aitattoomaker.databinding.FragmentCameraBinding
import com.basit.aitattoomaker.presentation.ai_tools.adapter.TattooAdapter
import com.basit.aitattoomaker.presentation.ai_tools.model.CameraTattoo
import com.basit.aitattoomaker.presentation.camera.result.ResultBottomSheet
import com.basit.aitattoomaker.presentation.utils.AppUtils
import com.basit.aitattoomaker.presentation.utils.CameraPermissionHelper
import com.basit.aitattoomaker.presentation.utils.DialogUtils
import com.basit.aitattoomaker.presentation.utils.DialogUtils.dialog
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import kotlin.math.abs

@ExperimentalGetImage
class CaptureConfirmFragment : Fragment() {
    private var binding: FragmentCameraBinding? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var isFlashOn = false
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var isFrontCamera = false
    private  var cameraControl: CameraControl?=null
    private  var cameraInfo: CameraInfo?=null
    private var imageCapture: ImageCapture? = null
    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }
    private val viewModel: CameraViewModel by viewModels { CameraViewModelFactory( requireActivity().application, TattooRepositoryImpl(requireContext()) ) }
    private var defaultTattoo: Bitmap? = null
    private val cameraTattooItems = listOf(
        CameraTattoo("Dragon", R.drawable.dragon),
        CameraTattoo("Dragon", R.drawable.dragon),
        CameraTattoo("Dragon",   R.drawable.dragon),
        CameraTattoo("Dragon",  R.drawable.dragon)
    )
    private lateinit var adapter: TattooAdapter
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let {
//            val androidId = Settings.Secure.getString(it.contentResolver, Settings.Secure.ANDROID_ID)
//            Log.e("checkUUID","AndroidID: $androidId")
            try {
                AppUtils.getMain(it)?.hidebottombar()
                setupRecycler()
                loadDefaultTattoo()
                setupClickListeners()
                if (CameraPermissionHelper.hasCameraPermission(requireContext())) {
                    startCamera()
                } else {
                    requestCameraPermission()
                }
            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }

    }

    private fun setupTattooCarousel(rv: RecyclerView, adapter: TattooAdapter) {
        val lm = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        rv.layoutManager = lm
        rv.adapter = adapter
        rv.setHasFixedSize(true)
        rv.clipToPadding = false
        rv.setPadding(dp(48), 0, dp(48), 0)

        rv.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val space = dp(12)
                outRect.right = space
                if (parent.getChildAdapterPosition(view) == 0) outRect.left = space
            }
        })

        // Ensure scale pivots from center (once per attached view)
        rv.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                view.pivotX = view.width / 2f
                view.pivotY = view.height / 2f
            }
            override fun onChildViewDetachedFromWindow(view: View) = Unit
        })

        val snap = LinearSnapHelper().also { it.attachToRecyclerView(rv) }

        val minScale = 0.75f
        val maxScale = 1.00f
        val minAlpha = 0.75f
        val maxAlpha = 1.00f

        fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
        fun smoothstep01(x: Float): Float {            // smooth 0..1 easing
            val t = x.coerceIn(0f, 1f)
            return t * t * (3 - 2 * t)                 // cubic smoothstep
        }

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val centerX = recyclerView.width / 2f
                for (i in 0 until recyclerView.childCount) {
                    val child = recyclerView.getChildAt(i) ?: continue
                    val childCenterX = (child.left + child.right) / 2f
                    val dist = abs(centerX - childCenterX)
                    val norm = (dist / centerX).coerceIn(0f, 1f)     // 0 at center → 1 at edge
                    val proximity = 1f - norm                        // 1 at center → 0 at edge

                    // Ease the proximity for smoother grow/shrink
                    val eased = smoothstep01(proximity)

                    val scale = lerp(minScale, maxScale, eased)
                    val alpha = lerp(minAlpha, maxAlpha, eased)

                    child.scaleX = scale
                    child.scaleY = scale
                    child.alpha  = alpha
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val view = snap.findSnapView(lm) ?: return
                    val pos = lm.getPosition(view)
                    if (pos != RecyclerView.NO_POSITION) {
                        // Tell adapter who’s centered (for ring/selection etc.)
                        adapter.setSelected(pos)

                        // Tiny settle anim: centered → exact max, others → min
                        for (i in 0 until recyclerView.childCount) {
                            val child = recyclerView.getChildAt(i) ?: continue
                            val targetScale = if (child == view) maxScale else minScale
                            val targetAlpha = if (child == view) maxAlpha else minAlpha
                            child.animate()
                                .scaleX(targetScale)
                                .scaleY(targetScale)
                                .alpha(targetAlpha)
                                .setDuration(120L)    // short & sweet
                                .start()
                        }
                    }
                }
            }
        })

        // Start centered on item 0 (or any index you want)
        rv.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                rv.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (adapter.itemCount == 0) return
                rv.post {
                    rv.smoothScrollToPosition(0)
                    adapter.setSelected(0)
                }
            }
        })
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()



    private fun Int?.orZero() = this ?: 0


    private fun setupRecycler(){
       /* binding?.apply {
            adapter = TattooAdapter { tattoo ->
                binding?.let { b ->
                    mActivity?.let { ctx ->
                        // Get drawable from id
                        val drawable = ContextCompat.getDrawable(ctx, tattoo.tattooId)?.mutate()
                        drawable?.alpha = 128  // set alpha

                        Glide.with(ctx)
                            .load(drawable)
                            .into(b.tattoo)
                    }

                }

            }
            rvTattoo.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rvTattoo.setHasFixedSize(true)
            rvTattoo.adapter = adapter
            adapter.submitList(tattooItems)
        }*/
        binding?.apply {
            adapter = TattooAdapter { tattoo ->
                // user tapped an item — you can also snap to it:
                val pos = adapter.currentList.indexOfFirst { it.name == tattoo.name }
                if (pos >= 0) rvTattoo.smoothScrollToPosition(pos)
                // do your click handling...
                binding?.let { b ->
                    mActivity?.let { ctx ->
                        // Get drawable from id
                        val drawable = ContextCompat.getDrawable(ctx, tattoo.id)?.mutate()
                        drawable?.alpha = 128  // set alpha

                        Glide.with(ctx)
                            .load(drawable)
                            .into(b.tattoo)
                    }

                }
            }
            setupTattooCarousel(rvTattoo, adapter)
            adapter.submitList(cameraTattooItems) // your list
        }

    }
    /** Load tattoo */
    private fun loadDefaultTattoo() {
        // Replace with repo/viewmodel logic if needed
        binding?.let { b ->
            mActivity?.let { ctx ->
                // Get drawable from id
                val drawable = ContextCompat.getDrawable(ctx, R.drawable.dragon)?.mutate()
                drawable?.alpha = 128  // set alpha
                Glide.with(ctx)
                    .load(drawable)
                    .into(b.tattoo)
            }

        }
    }

    /** Click listeners */
    private fun setupClickListeners() {
        binding?.apply {
            flipCamera?.setOnClickListener {
                bindCameraUseCases(true)
            }
            flash.setOnClickListener {
                toggleFlash()
            }
        }
        binding?.btnCapture?.setOnClickListener {
            mActivity?.let {
                DialogUtils.show(it, "Processing...")
                dialog?.show()
            }
            captureImage()
//            findNavController().navigate(
//            CameraScreenDirections.actionNavigationAicameraToNavigationAitools())
        }
//        binding?.btnGallery?.setOnClickListener {
//            mActivity?.let {
//                DialogUtils.show(it, "Processing...")
//                dialog?.show()
//            }
//            captureImage()
//        }
    }
    /** Toggle Flash */
    private fun toggleFlash() {
        if (isFrontCamera) return // no flash on front camera

        isFlashOn = !isFlashOn
        cameraControl?.enableTorch(isFlashOn)
        // Change ImageView drawable
        binding?.apply {
            Glide.with(flash)
                .load( if (isFlashOn) R.drawable.flash_on else R.drawable.flash_off)
                .into(flash)
        }
    }
    /** Tattoo gallery */
//    private fun openTattooGallery() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
//            Intent.setType = "image/*"
//            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png", "image/jpeg"))
//        }
//        startActivityForResult(intent, PICK_TATTOO_REQUEST)
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_TATTOO_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
//                binding?.slStickerLayout?.addSticker(bitmap)
            }
        }
    }

    /** Capture image */
    private fun captureImage() {
        imageCapture?.takePicture(
            ContextCompat.getMainExecutor(requireContext()), // or background executor
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val bitmap = imageProxy.toBitmap() // extension function converts ImageProxy -> Bitmap
                    imageProxy.close()
                    bitmap?.let {
                        val rotated = rotateBitmapIfNeeded(bitmap, imageProxy.imageInfo.rotationDegrees)
                        cameraExecutor.execute {
                            val finalBitmap = rotated?.let { binding?.overlayView?.getFinalBitmap(it) }
                            if(isAdded){
                                try {
                                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                                        finalBitmap?.let { showResultDialog(it) }
                                    }
                                } catch (e: Exception) {
                                   e.printStackTrace()
                                }
                            }
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Capture failed: ${exception.message}", exception)
                }
            }
        )
    }


    /** Merge tattoo with captured photo */
    private fun mergeTattoo(base: Bitmap, tattoo: Bitmap?, position: RectF?): Bitmap {
        val result = base.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        tattoo?.let {
            val src = Rect(0, 0, it.width, it.height)
            val dst = RectF(position ?: RectF(100f, 100f, 300f, 300f))
            canvas.drawBitmap(it, src, dst, null)
        }
        return result
    }

    /** Preview dialog */
    private fun showResultDialog(bitmap: Bitmap) {
        val uri = saveTempBitmap(bitmap) ?: run {
            Toast.makeText(requireContext(), "Failed to prepare preview", Toast.LENGTH_SHORT).show()
            return
        }
        ResultBottomSheet.newInstance(uri)
            .show(childFragmentManager, "ResultBottomSheet")
    }

    private fun saveTempBitmap(bitmap: Bitmap): Uri? {
        return try {
            val file = File(requireContext().cacheDir, "preview_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
        } catch (_: Exception) { null }
    }
    private fun rotateBitmapIfNeeded(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap

        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    /** Save to gallery */
    fun saveImageToGallery(bitmap: Bitmap) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Tattoo_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TattooApp")
        }
        val resolver = requireContext().contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            resolver.openOutputStream(it)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                Toast.makeText(requireContext(), "Saved to Gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Camera setup */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

//    private fun bindCameraUseCases() {
//        cameraProvider.unbindAll()
//
//        val preview = Preview.Builder()
//            .setTargetRotation(binding?.previewView?.display?.rotation?: Surface.ROTATION_90)
//            .build().apply {
//                surfaceProvider = binding?.previewView?.surfaceProvider
//            }
//
//        imageCapture = ImageCapture.Builder()
//            .setTargetRotation(binding?.previewView?.display?.rotation?: Surface.ROTATION_90)
//            .build()
//
//        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
//    }
    private fun bindCameraUseCases(flip: Boolean = false) {
        // Flip camera if requested
        if (flip) {
            isFrontCamera = !isFrontCamera
            cameraSelector = if (isFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
        }
        // Unbind old use cases
        cameraProvider?.unbindAll()

        // Setup preview
        val preview = Preview.Builder()
            .setTargetRotation(binding?.previewView?.display?.rotation ?: Surface.ROTATION_90)
            .build().apply {
                surfaceProvider = binding?.previewView?.surfaceProvider
            }

        // Setup image capture

         imageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .setTargetRotation(binding?.previewView?.display?.rotation ?: Surface.ROTATION_0)
        .build()


    // Bind to lifecycle
        val camera=cameraProvider?.bindToLifecycle(
            this,
            cameraSelector,
            preview,
            imageCapture
        )
    // Get control & info for flash
    cameraControl = camera?.cameraControl
    cameraInfo = camera?.cameraInfo

    // Disable flash icon for front camera
    binding?.flash?.visibility = if (isFrontCamera) View.INVISIBLE else View.VISIBLE
    }
    private fun requestCameraPermission() {
        CameraPermissionHelper.requestCameraPermission(requireActivity())
    }

    private fun observeViewModel() {
        viewModel.segmentationResult.observe(viewLifecycleOwner) { mask ->
            binding?.overlayView?.updateSegmentation(mask)
        }

//        viewModel.selectedTattoo.observe(viewLifecycleOwner) { tattoo ->
//            tattoo?.let { binding?.overlayView?.setTattoo(it) }
//        }

//        viewModel.errorState.observe(viewLifecycleOwner) { error ->
//            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraScreen"
        private const val PICK_TATTOO_REQUEST = 1001
    }
}