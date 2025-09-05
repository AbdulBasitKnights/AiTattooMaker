package com.basit.aitattoomaker.presentation.history

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.basit.aitattoomaker.databinding.FragmentHistoryBinding
import com.basit.aitattoomaker.presentation.history.model.Creation
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    var creations : List<Creation>?=null
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
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
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let {
            val directoryPath = File(it.filesDir, "TattooMe")

// Map the creations to the corresponding paths in the "TattooMe" folder
            creations = directoryPath.listFiles()?.map {
                // Create the Creation object with the file path as the imageUrl
                Creation(imageUrl = "file://${it.absolutePath}")
            } ?: listOf(
                Creation(imageUrl = "file:///android_asset/library/11.webp"),
                Creation(imageUrl = "file:///android_asset/library/1.webp"),
                Creation(imageUrl = "file:///android_asset/library/9.webp"),
                Creation(imageUrl = "file:///android_asset/library/2.webp"),
                Creation(imageUrl = "file:///android_asset/library/8.webp"),
                Creation(imageUrl = "file:///android_asset/library/3.webp"),
                Creation(imageUrl = "file:///android_asset/library/13.webp"),
                Creation(imageUrl = "file:///android_asset/library/4.webp"),
                Creation(imageUrl = "file:///android_asset/library/6.webp"),
                Creation(imageUrl = "file:///android_asset/library/5.webp"),
                Creation(imageUrl = "file:///android_asset/library/7.webp"),
            )
            if(isAdded){
                setupRecycler()
                // Complete the code here
            }
        }
    }

    private fun setupRecycler() {
        val adapter = CreationAdapter { creation ->
            creation.isSelected=true
            Toast.makeText(requireActivity(), "Clicked: ${creation.imageUrl}", Toast.LENGTH_SHORT).show()
        }
        binding.rvCreationList.adapter = adapter
        adapter.submitList(creations)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}