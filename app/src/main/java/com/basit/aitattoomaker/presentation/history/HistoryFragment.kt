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

class HistoryFragment : Fragment() {
    val creations = listOf(
        Creation(imageUrl = "file:///android_asset/library/11.png"),
        Creation(imageUrl = "file:///android_asset/library/1.png"),
        Creation(imageUrl = "file:///android_asset/library/9.png"),
        Creation(imageUrl = "file:///android_asset/library/2.png"),
        Creation(imageUrl = "file:///android_asset/library/8.png"),
        Creation(imageUrl = "file:///android_asset/library/3.png"),
        Creation(imageUrl = "file:///android_asset/library/13.png"),
        Creation(imageUrl = "file:///android_asset/library/4.png"),
        Creation(imageUrl = "file:///android_asset/library/6.png"),
        Creation(imageUrl = "file:///android_asset/library/5.png"),
        Creation(imageUrl = "file:///android_asset/library/7.png"),
    )
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
            if(isAdded){
                setupRecycler()
                // Complete the code here
            }
        }
    }

    private fun setupRecycler() {
        val adapter = CreationAdapter { creation ->
            Toast.makeText(requireActivity(), "Clicked: ${creation.id}", Toast.LENGTH_SHORT).show()
        }
        binding.rvCreationList.adapter = adapter
        adapter.submitList(creations)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}