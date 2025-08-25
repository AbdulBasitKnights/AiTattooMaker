package com.basit.aitattoomaker.presentation.ai_create.dialog

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.DialogResultPreviewBinding
import com.basit.aitattoomaker.databinding.DialogStylePreviewBinding
import com.basit.aitattoomaker.presentation.ai_create.adapter.StyleAdapter
import com.basit.aitattoomaker.presentation.ai_create.model.StyleItem
import com.basit.aitattoomaker.presentation.camera.CameraScreen
import com.basit.aitattoomaker.presentation.camera.result.ResultBottomSheet
import com.basit.aitattoomaker.presentation.utils.AppUtils
import com.basit.aitattoomaker.presentation.utils.DialogUtils
import com.basit.aitattoomaker.presentation.utils.styleLiveData
import com.basit.aitattoomaker.presentation.utils.style_list
import com.basit.library.stickerview.StickerFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StyleBottomSheet : BottomSheetDialogFragment() {

    interface Callback {
        fun onDone(selected: StyleItem?)
        fun onCancel()
    }
    companion object {
        fun newInstance(): StyleBottomSheet {
            return StyleBottomSheet()
        }
    }

    var callback: Callback? = null

    private lateinit var adapter: StyleAdapter
    private var _binding: DialogStylePreviewBinding? = null
    private val binding get() = _binding!!

    // Keep a copy of original selection
    private lateinit var tempList: ArrayList<StyleItem>

    override fun getTheme(): Int = R.style.ThemeOverlay_App_RoundedTopSheet

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
        dialog.behavior.isFitToContents = true
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogStylePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make a copy of the global list, so cancel does not mutate it
        tempList = ArrayList(style_list.map { it.copy() })

        setupRecycler()

        binding.cancel.setOnClickListener {
            callback?.onCancel()
            dismiss()
        }

        binding.done.setOnClickListener {
            // Update the global list only on Done
            style_list.forEachIndexed { index, _ ->
                style_list[index].isSelected = tempList[index].isSelected
            }

            val selectedItem = style_list.firstOrNull { it.isSelected }
            callback?.onDone(selectedItem)
            dismiss()
        }
    }

    private fun setupRecycler() {
        adapter = StyleAdapter { selected ->
            tempList.forEach { it.isSelected = it.id == selected.id }
            adapter.submitList(tempList.toList()) // update adapter
        }

        binding.rvStyle.adapter = adapter

        // Show temp list initially
        adapter.submitList(tempList.toList())
        adapter.setInitialSelection(tempList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



