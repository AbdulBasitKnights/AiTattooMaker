package com.basit.aitattoomaker.presentation.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.ExperimentalGetImage
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.basit.aitattoomaker.databinding.DialogTattooGalleryBinding
import com.basit.aitattoomaker.presentation.camera.adapter.CameraTattooAdapter

@ExperimentalGetImage
class TattooGalleryDialog : DialogFragment() {
    private var _binding: DialogTattooGalleryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CameraViewModel by viewModels()

    private val adapter = CameraTattooAdapter { tattoo ->
        viewModel.selectTattoo(tattoo)
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTattooGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.recyclerView.adapter = adapter

        viewModel.tattoos.observe(viewLifecycleOwner) { tattoos ->
            adapter.submitList(tattoos)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = TattooGalleryDialog()
    }
}