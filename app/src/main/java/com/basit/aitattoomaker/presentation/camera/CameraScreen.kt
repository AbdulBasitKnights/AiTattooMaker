package com.basit.aitattoomaker.presentation.camera

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.ads.AdsManager
import com.basit.aitattoomaker.ads.AdsManager.isShowingAd
import com.basit.aitattoomaker.ads.AdsManager.loadInterstitialAdAfterSplash
import com.basit.aitattoomaker.databinding.FragmentCameraBinding
import com.basit.aitattoomaker.extension.showDiscardDialog
import com.basit.aitattoomaker.extension.showExitDialog
import com.basit.aitattoomaker.extension.uriToBitmap
import com.basit.aitattoomaker.presentation.ai_tools.AiToolsViewModel
import com.basit.aitattoomaker.presentation.camera.adapter.CameraTattooAdapter
import com.basit.aitattoomaker.presentation.camera.dialog.InfoBottomSheet
import com.basit.aitattoomaker.presentation.utils.AppUtils
import com.basit.aitattoomaker.presentation.utils.AppUtils.isRational
import com.basit.aitattoomaker.presentation.utils.AppUtils.tattooPath
import com.basit.aitattoomaker.presentation.utils.CameraPermissionHelper
import com.basit.aitattoomaker.presentation.utils.DialogUtils
import com.basit.aitattoomaker.presentation.utils.DialogUtils.dialog
import com.basit.aitattoomaker.presentation.utils.capturedBitmap
import com.basit.aitattoomaker.presentation.utils.selectedTattoo
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
@AndroidEntryPoint
@ExperimentalGetImage
class CameraScreen : Fragment() {
    private var binding: FragmentCameraBinding? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var isFlashOn = false
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var isFrontCamera = false
    private  var cameraControl: CameraControl?=null
    private  var cameraInfo: CameraInfo?=null
    private val tattooViewModel: AiToolsViewModel by activityViewModels()
    private var imageCapture: ImageCapture? = null
    private val cameraExecutor by lazy {
        Executors.newSingleThreadExecutor()
    }
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                isRational=false
                isShowingAd=true
                // ✅ Permission granted
                startCamera()
            } else {
                isShowingAd=false
                // ❌ Permission denied
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    // User denied but didn’t check "Don’t ask again"
                    Toast.makeText(requireContext(), "Camera permission is required!", Toast.LENGTH_SHORT).show()
                } else {
                    // User denied + "Don’t ask again"
                    showPermissionSettingsDialog()
                }
            }
        }
    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Camera Permission Needed")
            .setMessage("Please enable Camera permission in Settings to use this feature.")
            .setPositiveButton("Go to Settings") { _, _ ->
                isRational=true
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", { dialog, _ ->
                mActivity?.finish()
            })
            .show()
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                mActivity?.uriToBitmap(uri)?.let {
                    showResultDialog(it)
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
    private val pickLegacyImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    mActivity?.uriToBitmap(uri)?.let {
                        showResultDialog(it)
                    }
                }
            }
        }

    //    private val viewModel: CameraViewModel by viewModels { CameraViewModelFactory( requireActivity().application, TattooRepositoryImpl(requireContext()) ) }
//    private var defaultTattoo: Bitmap? = null
    private lateinit var adapter: CameraTattooAdapter
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
                loadInterstitialAdAfterSplash(it,resources.getString(R.string.inter_af_home_hf),resources.getString(R.string.inter_af_home),{},{},{})
                mActivity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
                    mActivity?.showExitDialog(
                        onDiscard = {
                            // User clicked discard, handle accordingly
                            mActivity?.finish()
//                            findNavController().popBackStack(R.id.navigation_aicreate,false)
                        },
                        onNotNow = {
                            // User clicked not now, just dismiss dialog
                        }
                    )
                }
                binding?.apply {
                    flash.setImageResource(if (isFlashOn) R.drawable.flash_on else R.drawable.flash_off)
                    mActivity?.let { ctx ->
                        Glide.with(ctx)
                            .load(tattooPath)
                            .into(tattoo)
                    }
                }
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
            adapter = CameraTattooAdapter { tattoo ->
                // do your click handling...
                tattooPath=tattoo.imageUrl
                selectedTattoo=tattoo.id
                binding?.let { b ->
                    mActivity?.let { ctx ->
                    /*    // Get drawable from id
                        val drawable = ContextCompat.getDrawable(ctx, tattoo.id)?.mutate()
                        drawable?.alpha = 128  // set alpha*/
                        Glide.with(ctx)
                            .load(tattoo.imageUrl)
                            .into(b.tattoo)
                    }

                }
            }
            binding?.rvTattoo?.adapter = adapter
            tattooViewModel.loadTattoos()
            try {
                tattooViewModel.library.observe(viewLifecycleOwner) {
                    if(!it.isNullOrEmpty()){
                        adapter.submitList(it.toList()) // your list
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
    /** Load tattoo */
    private fun loadDefaultTattoo() {
        // Replace with repo/viewmodel logic if needed
        binding?.let { b ->
            mActivity?.let { ctx ->
                // Get drawable from id
                Glide.with(ctx)
                    .load(tattooPath)
                    .into(b.tattoo)
            }

        }
    }

    /** Click listeners */
    private fun setupClickListeners() {
        binding?.apply {
            flipCamera?.setOnClickListener {
                if (CameraPermissionHelper.hasCameraPermission(requireContext())) {
                    bindCameraUseCases(true)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
            flash.setOnClickListener {
                if (CameraPermissionHelper.hasCameraPermission(requireContext())) {
                    toggleFlash()
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }

            }
            cross?.setOnClickListener {
                mActivity?.showExitDialog(
                    onDiscard = {
                        // User clicked discard, handle accordingly
                        mActivity?.finish()
//                            findNavController().popBackStack(R.id.navigation_aicreate,false)
                    },
                    onNotNow = {
                        // User clicked not now, just dismiss dialog
                    }
                )
             /*   try {
                    mActivity?.finish()
//                    findNavController().popBackStack(R.id.navigation_aicreate,false)
                }
                catch (e:Exception){
                    e.printStackTrace()
                }*/
            }
            setting?.setOnClickListener {
                findNavController().navigate(CameraScreenDirections.actionNavigationAicameraToNavigationSettings())
            }
            library.setOnClickListener {
                library.setTextColor(resources.getColor(R.color.white))
                history.setTextColor(resources.getColor(R.color.lightGrey))
                lifecycleScope.launch(Dispatchers.Main) {
                    adapter.submitList(tattooViewModel.library.value?.toList())
                }
            }
            history.setOnClickListener {
                library.setTextColor(resources.getColor(R.color.lightGrey))
                history.setTextColor(resources.getColor(R.color.white))
                lifecycleScope.launch(Dispatchers.Main) {
                    adapter.submitList(tattooViewModel.history.value?.toList())
                }
            }
            gallery.setOnClickListener {
                openPicker()
            }
            info.setOnClickListener {
                try {
                    val sheet = InfoBottomSheet.newInstance()
                    sheet.callback = object : InfoBottomSheet.Callback {
                        override fun onCancel() {
//                        Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show()
                        }
                    }
                    sheet.show(parentFragmentManager, "InfoBottomSheet")
                }
                catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
        binding?.btnCapture?.setOnClickListener {
            if (CameraPermissionHelper.hasCameraPermission(requireContext())) {
                mActivity?.let {
                    DialogUtils.show(it, "Capturing...")
                    dialog?.show()
                }
                captureImage()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

/*    fun openPicker() {
        openPicker()
//        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }*/

    /** Toggle Flash */
    private fun toggleFlash() {
        if (isFrontCamera) return // no flash on front camera

        isFlashOn = !isFlashOn
        cameraControl?.enableTorch(isFlashOn)
        // Change ImageView drawable
        binding?.apply {
            flash.setImageResource(if (isFlashOn) R.drawable.flash_on else R.drawable.flash_off)
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
        imageCapture?.takePicture(
            ContextCompat.getMainExecutor(requireContext()), // callback on main
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        val bitmap = image.toBitmap()
                        val rotated = rotateBitmapIfNeeded(bitmap, image.imageInfo.rotationDegrees)
                        withContext(Dispatchers.Main) {
                            val finalBitmap = rotated?.let { binding?.overlayView?.getFinalBitmap(it) }
                            finalBitmap?.let { showResultDialog(it) }
                        }
                        image.close()
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    exc.printStackTrace()
                }
            }
        )
    }

    /** Preview dialog */
    private fun showResultDialog(bitmap: Bitmap) {
//        capturedUri = saveTempBitmap(bitmap) ?: run {
//            Toast.makeText(requireContext(), "Failed to prepare preview", Toast.LENGTH_SHORT).show()
//            return
//        }
        capturedBitmap=bitmap
        dialog?.dismiss()
        mActivity?.let { activity->
            AdsManager.showInterstitialAfterSplash(activity, AdsManager.inter_af_home_hf?: AdsManager.inter_af_home,
                if(AdsManager.inter_af_home_hf!=null)true else false,
                {bitmap?.let { bitmap ->
                    findNavController().navigate(CameraScreenDirections.actionNavigationAicameraToNavigationAitools())
                }},
                {
                    bitmap?.let { bitmap ->
                        findNavController().navigate(CameraScreenDirections.actionNavigationAicameraToNavigationAitools())
                    }
                },
                {},
                {
                    bitmap?.let { bitmap ->
                        findNavController().navigate(CameraScreenDirections.actionNavigationAicameraToNavigationAitools())
                    }
                })
        }
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

        val matrix = android.graphics.Matrix()
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
    fun openPicker() {
        when {
            // Android 13+ → New Photo Picker
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                pickMedia.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }

            // Android 10–12 → Legacy ACTION_PICK
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "image/*"
                }
                pickLegacyImage.launch(intent)
            }
            else -> {
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
        isShowingAd=true
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

 /*   private fun observeViewModel() {
        viewModel.segmentationResult.observe(viewLifecycleOwner) { mask ->
            binding?.overlayView?.updateSegmentation(mask)
        }
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        if(isRational){
            if (CameraPermissionHelper.hasCameraPermission(requireContext())) {
                startCamera()
            } else {
                requestCameraPermission()
            }
            isRational=false
        }
    }

    companion object {
        private const val TAG = "CameraScreen"
        private const val PICK_TATTOO_REQUEST = 1001
        private const val REQUEST_PICK_IMAGE = 101
        private const val REQUEST_STORAGE_PERMISSION = 102
    }
}