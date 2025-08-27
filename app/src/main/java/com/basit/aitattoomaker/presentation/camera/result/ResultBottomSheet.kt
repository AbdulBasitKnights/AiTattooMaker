package com.basit.aitattoomaker.presentation.camera.result

import android.app.Dialog
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.core.os.bundleOf
import androidx.core.view.drawToBitmap
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.DialogResultPreviewBinding
import com.basit.aitattoomaker.presentation.camera.CameraScreen
import com.basit.aitattoomaker.presentation.utils.AppUtils
import com.basit.aitattoomaker.presentation.utils.AppUtils.tattooPath
import com.basit.aitattoomaker.presentation.utils.DialogUtils
import com.basit.library.stickerview.StickerFactory

class ResultBottomSheet : com.google.android.material.bottomsheet.BottomSheetDialogFragment() {

    companion object {
        private const val ARG_URI = "arg_uri"

        fun newInstance(imageUri: Uri): ResultBottomSheet = ResultBottomSheet().apply {
            arguments = bundleOf(ARG_URI to imageUri.toString())
        }
    }

    override fun getTheme(): Int = R.style.ThemeOverlay_App_RoundedTopSheet

    private var _binding: DialogResultPreviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as com.google.android.material.bottomsheet.BottomSheetDialog
        dialog.behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
        dialog.behavior.isFitToContents = true
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogResultPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(ExperimentalGetImage::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DialogUtils?.dialog?.dismiss()
        // Load bitmap from the Uri we pass (safer than putting a large Bitmap in args)
        val uriStr = requireArguments().getString(ARG_URI)!!
        val uri = Uri.parse(uriStr)

        val bitmap = requireContext().contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
        if (bitmap != null) {
            binding.imgResult.setImageBitmap(bitmap)
        }

        // Add/update sticker
        StickerFactory.currentSticker = StickerFactory.createStickerFromAsset(
            context = requireContext(),
            assetPath = tattooPath,  // can be "library/dragon.png" OR "file:///android_asset/library/dragon.png"
            alpha = 128
        )
        StickerFactory.currentSticker?.let {
            binding?.slStickerLayout?.addSticker(it)
        }

        binding.btnRetake.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener {
            // delegate back to parent, or save here
            (parentFragment as? CameraScreen)?.saveImageToGallery(
                binding.imgResult.drawToBitmap()
            )
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
