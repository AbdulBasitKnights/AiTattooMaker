package com.basit.aitattoomaker.presentation.camera

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.data.repo.TattooRepositoryImpl
import com.basit.aitattoomaker.databinding.FragmentCameraBinding
import com.basit.aitattoomaker.presentation.ai_tools.adapter.TattooAdapter
import com.basit.aitattoomaker.presentation.ai_tools.model.Tattoo
import com.basit.aitattoomaker.presentation.camera.result.ResultBottomSheet
import com.basit.aitattoomaker.presentation.utils.AppUtils
import com.basit.aitattoomaker.presentation.utils.AppUtils.decodeAndFixOrientation
import com.basit.aitattoomaker.presentation.utils.AppUtils.tattooID
import com.basit.aitattoomaker.presentation.utils.CameraPermissionHelper
import com.basit.aitattoomaker.presentation.utils.DialogUtils
import com.basit.aitattoomaker.presentation.utils.DialogUtils.dialog
import com.basit.library.stickerview.StickerFactory
import com.basit.library.stickerview.StickerLayout
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

@ExperimentalGetImage
class CameraScreen : Fragment() {
    private var binding: FragmentCameraBinding? = null
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageCapture: ImageCapture
    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }
    private val viewModel: CameraViewModel by viewModels { CameraViewModelFactory( requireActivity().application, TattooRepositoryImpl(requireContext()) ) }
    private var defaultTattoo: Bitmap? = null
    private val tattooItems = listOf(
        Tattoo("Dragon", R.drawable.dragon),
        Tattoo("Flower", R.drawable.flower),
        Tattoo("Fire",   R.drawable.tattoo),
        Tattoo("Heart",  R.drawable.heart)
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
    private fun setupRecycler(){
        binding?.apply {

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
        }

    }
    /** Load tattoo */
    private fun loadDefaultTattoo() {
        // Replace with repo/viewmodel logic if needed
        binding?.let { b ->
            mActivity?.let { ctx ->
                // Get drawable from id
                val drawable = ContextCompat.getDrawable(ctx, R.drawable.tattoo)?.mutate()
                drawable?.alpha = 128  // set alpha
                Glide.with(ctx)
                    .load(drawable)
                    .into(b.tattoo)
            }

        }
    }

    /** Click listeners */
    private fun setupClickListeners() {
//        binding.btnGallery.setOnClickListener { openTattooGallery() }
        binding?.btnGallery?.setOnClickListener {
            mActivity?.let {
                DialogUtils.show(it, "Processing...")
                dialog?.show()
            }
            captureImage()
        }
    }

    /** Tattoo gallery */
    private fun openTattooGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png", "image/jpeg"))
        }
        startActivityForResult(intent, PICK_TATTOO_REQUEST)
    }

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
        val photoFile = File(
            requireContext().cacheDir,
            "temp_${System.currentTimeMillis()}.jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val corrected = decodeAndFixOrientation(photoFile)
                    val finalBitmap = corrected?.let { binding?.overlayView?.getFinalBitmap(it) }
                    requireActivity().runOnUiThread {
                        finalBitmap?.let { showResultDialog(it) }
                    }
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
//    private fun showResultDialog(bitmap: Bitmap) {
//        val dialog = Dialog(requireContext()).apply {
//            setContentView(R.layout.dialog_result_preview)
//            findViewById<ImageView>(R.id.imgResult).setImageBitmap(bitmap)
//            StickerFactory.currentSticker =
//                StickerFactory.createSticker(context = requireContext(), drawableId = tattooID, alpha = 128)
//            findViewById<StickerLayout>(R.id.sl_sticker_layout).addOrUpdateSticker(StickerFactory.currentSticker)
//            findViewById<Button>(R.id.btnSave).setOnClickListener {
//                saveImageToGallery(bitmap)
//                dismiss()
//            }
//            findViewById<Button>(R.id.btnRetake).setOnClickListener { dismiss() }
//        }
//        dialog.show()
//    }
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

    private fun bindCameraUseCases() {
        cameraProvider.unbindAll()

        val preview = Preview.Builder()
            .setTargetRotation(binding?.previewView?.display?.rotation?: Surface.ROTATION_90)
            .build().apply {
                surfaceProvider = binding?.previewView?.surfaceProvider
            }

        imageCapture = ImageCapture.Builder()
            .setTargetRotation(binding?.previewView?.display?.rotation?: Surface.ROTATION_90)
            .build()

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
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