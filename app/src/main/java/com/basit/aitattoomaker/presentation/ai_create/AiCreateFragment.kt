package com.basit.aitattoomaker.presentation.ai_create

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.FragmentAiCreateBinding
import com.basit.aitattoomaker.presentation.ai_create.adapter.StyleAdapter
import com.basit.aitattoomaker.presentation.ai_create.model.StyleItem

class AiCreateFragment : Fragment(R.layout.fragment_ai_create) {

    private var _binding: FragmentAiCreateBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: StyleAdapter

    // dp helper
    private val Int.dp get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAiCreateBinding.bind(view)
        requireActivity()?.let {
            btnClicks()
            setupRecycler()
            setupPromptField()
            setupImeAwareScrolling()
            setupOutsideTapToClearFocus()
        }

    }

    private fun setupRecycler() = with(binding) {

        adapter = StyleAdapter { selected ->
            Toast.makeText(requireContext(), "Selected: ${selected.title}", Toast.LENGTH_SHORT).show()
        }
        rvStyles.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvStyles.adapter = adapter

        // Dummy data
        adapter.submitList(
            listOf(
                StyleItem("1", "Fire", R.drawable.tattoo),
                StyleItem("2", "Dragon", R.drawable.dragon),
                StyleItem("3", "Flower", R.drawable.flower),
                StyleItem("4", "Heart", R.drawable.heart)
            )
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPromptField() = with(binding) {
        // EditText inner scrolling (only steal touch from parent when it can scroll)
        etPrompt.isVerticalScrollBarEnabled = true
        etPrompt.overScrollMode = View.OVER_SCROLL_ALWAYS
        etPrompt.setOnTouchListener { v, ev ->
            if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                // decide if we need to disallow based on scrollability
                val allowInnerScroll = etPrompt.canScrollVertically(-1) || etPrompt.canScrollVertically(1)
                v.parent.requestDisallowInterceptTouchEvent(allowInnerScroll)
            }
            if (ev.actionMasked == MotionEvent.ACTION_UP || ev.actionMasked == MotionEvent.ACTION_CANCEL) {
                v.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }

        // Enable/disable Create button based on text presence
        etPrompt.addTextChangedListener { btnCreate.isEnabled = !it.isNullOrBlank()
            if(!it.isNullOrBlank()){
                btnClear?.visibility= View.VISIBLE
            }
            else{
                btnClear?.visibility= View.GONE
            }
        }

        btnCreate.setOnClickListener {
            findNavController().navigate(
                AiCreateFragmentDirections.actionNavigationAicreateToNavigationAitools()
            )
        }
        btnClear.setOnClickListener {
            binding.etPrompt.text?.clear()
        }
    }
    override fun onResume() {
        super.onResume()
        try {
            requireActivity().window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        // Reset back to resize if needed elsewhere
        try {
            requireActivity().window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    /**
     * Keyboard-aware padding + fast snapping to EditText when IME shows,
     * and clear focus when IME hides.
     */
    private fun setupImeAwareScrolling() = with(binding) {
        val scroll = rootScroll
        val edit = etPrompt
        val root = requireView()

        // Apply system bars insets once, keep bottom via animation callback
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, sys.top, v.paddingRight, v.paddingBottom)
            insets
        }

        ViewCompat.setWindowInsetsAnimationCallback(
            root,
            object : WindowInsetsAnimationCompat.Callback(
                DISPATCH_MODE_CONTINUE_ON_SUBTREE
            ) {
                private var lastImeVisible = false

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    running: List<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                    val sysBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                    val targetBottom = maxOf(imeBottom, sysBottom)

                    // fast, jank-free bottom padding update
                    scroll.setPadding(scroll.paddingLeft, scroll.paddingTop, scroll.paddingRight, targetBottom)

                    val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

                    if (imeVisible && !lastImeVisible) {
                        // Snap scroll so cursor isn't hidden by keyboard
                        scroll.post {
                            scroll.isSmoothScrollingEnabled = true
                            scroll.smoothScrollTo(0, edit.bottom + 32.dp)
                        }
                    } else if (!imeVisible && lastImeVisible) {
                        // Keyboard just hid -> clear focus
                        edit.clearFocus()
                    }

                    lastImeVisible = imeVisible
                    return insets
                }
            }
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupOutsideTapToClearFocus() = with(binding) {
        root.setOnTouchListener { _, _ ->
            if (etPrompt.hasFocus()) {
                etPrompt.clearFocus()
                hideKeyboardFrom(etPrompt)
            }
            false
        }
    }

    private fun hideKeyboardFrom(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun AiCreateFragment.btnClicks()= with(binding) {
      btnVariations?.setOnClickListener {  }
      btnCanvas?.setOnClickListener {  }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


