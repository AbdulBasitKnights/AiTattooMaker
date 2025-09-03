package com.basit.aitattoomaker.presentation.ai_tools

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.basit.aitattoomaker.databinding.FragmentSaveBinding
import com.basit.aitattoomaker.extension.showDownloadDialog
import com.basit.aitattoomaker.presentation.utils.DialogUtils
import com.basit.aitattoomaker.presentation.utils.DialogUtils.dialog
import com.basit.aitattoomaker.presentation.utils.editedBitmap
import com.basit.aitattoomaker.presentation.utils.tattooCreation
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@AndroidEntryPoint
class SaveFragment : Fragment() {

    private var binding: FragmentSaveBinding? = null
    private var mActivity: FragmentActivity?=null
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
        binding = FragmentSaveBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        mActivity?.let {activity->
            try {
                val uri=saveBitmapToCache(activity, editedBitmap!!)
                tattooCreation.postValue(false)
                binding?.apply {
                    Glide.with(activity)
                        .load(editedBitmap)
                        .into(editedImage)
                    download?.setOnClickListener {
                        saveToGallery()
                    }
                    share?.setOnClickListener {
                        shareUri(activity, uri)
                    }
                    cross.setOnClickListener {
                        findNavController().popBackStack()
                    }
                }
            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }

    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String = "sticker.png"): Uri {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, fileName)

        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }

        // Get URI via FileProvider
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
    fun shareUri(context: Context, uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Image"))
        } catch (e: Exception) {
           e.printStackTrace()
        }
    }

    private fun saveToGallery() {
        // Ensure current edits applied visually
        mActivity?.let {
            DialogUtils.show(it, "Saving...")
        }
        dialog?.show()
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            editedBitmap?.let {
                val ok = saveBitmapToGallery(it, "tattoo_result_${System.currentTimeMillis()}.png")
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

}
