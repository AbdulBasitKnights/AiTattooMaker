package com.basit.aitattoomaker.presentation.camera

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.data.repo.TattooRepositoryImpl
import com.basit.aitattoomaker.databinding.FragmentCameraBinding
import com.basit.aitattoomaker.presentation.utils.CameraPermissionHelper
import java.io.File
import java.util.concurrent.Executors

@ExperimentalGetImage
class CameraScreen : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageCapture: ImageCapture
    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }
    private val viewModel: CameraViewModel by viewModels { CameraViewModelFactory( requireActivity().application, TattooRepositoryImpl(requireContext()) ) }
    private var defaultTattoo: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadDefaultTattoo()
        setupClickListeners()

        if (CameraPermissionHelper.hasCameraPermission(requireContext())) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    /** Load tattoo */
    private fun loadDefaultTattoo() {
        // Replace with repo/viewmodel logic if needed
        defaultTattoo = BitmapFactory.decodeResource(resources, R.drawable.tattoo)
        defaultTattoo?.let { binding.overlayView.setTattoo(it) }
    }

    /** Click listeners */
    private fun setupClickListeners() {
//        binding.btnGallery.setOnClickListener { openTattooGallery() }
        binding.btnGallery.setOnClickListener { captureImage() }
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
                binding.overlayView.setTattoo(bitmap)
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
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    val finalBitmap = binding.overlayView.getFinalBitmap(bitmap)

                    requireActivity().runOnUiThread {
                        showResultDialog(finalBitmap)
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
    private fun showResultDialog(bitmap: Bitmap) {
        val dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_result_preview)
            findViewById<ImageView>(R.id.imgResult).setImageBitmap(bitmap)

            findViewById<Button>(R.id.btnSave).setOnClickListener {
                saveImageToGallery(bitmap)
                dismiss()
            }
            findViewById<Button>(R.id.btnRetake).setOnClickListener { dismiss() }
        }
        dialog.show()
    }

    /** Save to gallery */
    private fun saveImageToGallery(bitmap: Bitmap) {
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
            .setTargetRotation(binding.previewView.display.rotation)
            .build().apply {
                setSurfaceProvider(binding.previewView.surfaceProvider)
            }

        imageCapture = ImageCapture.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
    }

    private fun requestCameraPermission() {
        CameraPermissionHelper.requestCameraPermission(requireActivity())
    }

    private fun observeViewModel() {
        viewModel.segmentationResult.observe(viewLifecycleOwner) { mask ->
            binding.overlayView.updateSegmentation(mask)
        }

        viewModel.selectedTattoo.observe(viewLifecycleOwner) { tattoo ->
            tattoo?.let { binding.overlayView.setTattoo(it) }
        }

//        viewModel.errorState.observe(viewLifecycleOwner) { error ->
//            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }

    companion object {
        private const val TAG = "CameraScreen"
        private const val PICK_TATTOO_REQUEST = 1001
    }
}