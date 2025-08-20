package com.basit.aitattoomaker.presentation.ai_create

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.FragmentAiCreateBinding
import com.basit.aitattoomaker.presentation.ai_create.adapter.StyleAdapter
import com.basit.aitattoomaker.presentation.ai_create.model.StyleItem

class AiCreateFragment : Fragment(R.layout.fragment_ai_create) {

    private var _binding: FragmentAiCreateBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: StyleAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAiCreateBinding.bind(view)
        setupRecycler()
        setupTextWatcher()
    }

    private fun setupRecycler() {
        adapter = StyleAdapter { selected ->
            Toast.makeText(requireContext(), "Selected: ${selected.title}", Toast.LENGTH_SHORT)
                .show()
        }
        binding.rvStyles.adapter = adapter

        // Dummy data
        adapter.submitList(
            listOf(
                StyleItem("1", "Fire", R.drawable.tattoo),
                StyleItem("2", "Dragon", R.drawable.dragon),
                StyleItem("3", "Flower", R.drawable.flower)
            )
        )
    }

    private fun setupTextWatcher() {
        binding.etPrompt.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
        binding.etPrompt.addTextChangedListener {
            binding.btnCreate.isEnabled = !it.isNullOrBlank()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
