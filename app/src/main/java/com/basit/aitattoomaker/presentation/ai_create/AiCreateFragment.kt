package com.basit.aitattoomaker.presentation.ai_create

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.FragmentAiCreateBinding
import com.basit.aitattoomaker.extension.dp
import com.basit.aitattoomaker.extension.observeKeyboardLegacy
import com.basit.aitattoomaker.extension.setDrawableTint
import com.basit.aitattoomaker.extension.setDrawableWithTint
import com.basit.aitattoomaker.presentation.ai_create.adapter.StyleAdapter
import com.basit.aitattoomaker.presentation.ai_create.dialog.AiCreationDialog
import com.basit.aitattoomaker.presentation.ai_create.dialog.StyleBottomSheet
import com.basit.aitattoomaker.presentation.ai_create.model.StyleItem
import com.basit.aitattoomaker.presentation.camera.result.ResultBottomSheet
import com.basit.aitattoomaker.presentation.utils.AppUtils
import com.basit.aitattoomaker.presentation.utils.AppUtils.tattooPrompts
import com.basit.aitattoomaker.presentation.utils.DialogUtils.creationDialog
import com.basit.aitattoomaker.presentation.utils.DialogUtils.showCreationDialog
import com.basit.aitattoomaker.presentation.utils.GradientStrokeDrawable
import com.basit.aitattoomaker.presentation.utils.styleLiveData
import com.basit.aitattoomaker.presentation.utils.style_list
import com.basit.aitattoomaker.presentation.utils.tattooCreation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@AndroidEntryPoint
class AiCreateFragment : Fragment(R.layout.fragment_ai_create) {

    private var binding: FragmentAiCreateBinding? = null
    companion object {
        var selectedItemIdPosition: Int? = 1
        var selectedItemIdPositionCanvas: Int? = 1
    }
    private lateinit var adapter: StyleAdapter


    // dp helper
    private val Int.dp get() = (this * Resources.getSystem().displayMetrics.density).toInt()
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
    ): View? {
        binding = FragmentAiCreateBinding.inflate(inflater, container, false)
        return binding?.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAiCreateBinding.bind(view)
        mActivity?.let {
            btnClicks()
            showCreationDialog(it)
            mActivity?.observeKeyboardLegacy { isVisible ->
                if (isVisible) {
                    // Keyboard is shown
                    binding?.apply {
                        promptCard.background = GradientStrokeDrawable(
                            radiusPx = root.dp(10),
                            strokePx = root.dp(2),
                            startColor = ContextCompat.getColor(root.context, R.color.colorprimary),
                            endColor = ContextCompat.getColor(root.context, R.color.colorsecondary),
                            angleDeg = 0f,  // 0 = left→right gradient
                            fillColor  = Color.TRANSPARENT
                        )
                    }

                } else {
                    // Keyboard is hidden
                    binding?.apply {
                        promptCard.background = GradientStrokeDrawable(
                            radiusPx = root.dp(10),
                            strokePx = root.dp(2),
                            startColor = ContextCompat.getColor(root.context, R.color.grey),
                            endColor = ContextCompat.getColor(root.context, R.color.grey),
                            angleDeg = 0f,  // 0 = left→right gradient
                            fillColor  = Color.TRANSPARENT
                        )
                    }
                }
            }
            setupRecycler()
            setupPromptField()
            setupImeAwareScrolling()
            setupOutsideTapToClearFocus()
            tattooCreation?.observe(viewLifecycleOwner){
                if(it==true){
                    creationDialog?.dismiss()
                    try {
                        findNavController().navigate(
                            AiCreateFragmentDirections.actionNavigationAicreateToResultScreen()
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    tattooCreation.postValue(false)
                }
            }
        }

    }

    private fun setupRecycler() {
        binding?.apply {
            adapter = StyleAdapter { selected ->
                style_list.forEach { it.isSelected = it.id == selected.id }
                styleLiveData.postValue(style_list)
//                Toast.makeText(requireContext(), "Selected: ${selected.title}", Toast.LENGTH_SHORT).show()
            }
            rvStyles.adapter = adapter
            styleLiveData?.observe(viewLifecycleOwner){
                if(!it.isNullOrEmpty()){
                    adapter.submitList(it)
                    adapter.setInitialSelection(it)
                }
            }
            // Dummy data

        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPromptField(){
        binding?.apply {
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
                    btnClear.visibility= View.VISIBLE
                }
                else{
                    btnClear.visibility= View.GONE
                }
            }
            etPrompt.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    view.post {
                        rootScroll.smoothScrollTo(0, view.bottom)
                    }
                }
            }

            btnCreate.setOnClickListener {
                    creationDialog?.show()
                lifecycleScope.launch {
                    delay(7000)
                    tattooCreation.postValue(true)
                 }
            }
            btnClear.setOnClickListener {
                binding?.etPrompt?.text?.clear()
            }
        }

    }
    override fun onResume() {
        super.onResume()
        try {
            mActivity?.let {
                AppUtils.getMain(it)?.showbottombar()
            }
            mActivity?.window?.setSoftInputMode(
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
            mActivity?.window?.setSoftInputMode(
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
    private fun setupImeAwareScrolling(){
        binding?.apply {
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

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupOutsideTapToClearFocus(){
        binding?.apply {
            root.setOnTouchListener { _, _ ->
                if (etPrompt.hasFocus()) {
                    etPrompt.clearFocus()
                    hideKeyboardFrom(etPrompt)
                }
                false
            }
        }

    }

    private fun hideKeyboardFrom(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun btnClicks(){
        binding?.apply {
            btnVariations.setOnClickListener {
                showCustomPopupVariations(btnVariations)
            }
            pro.setOnClickListener {
                Toast.makeText(mActivity, "Pro Button", Toast.LENGTH_SHORT).show()
            }
            promptCard.setOnClickListener {
                etPrompt.requestFocus()
                etPrompt.showKeyboard()
            }

            btnSurpriseMe?.setOnClickListener {
                val randomPrompt = tattooPrompts.random()
                etPrompt.setText(randomPrompt)
            }
            btnCanvas.setOnClickListener { showCustomPopupCanvas(btnCanvas) }
            seeAll.setOnClickListener {
                val sheet = StyleBottomSheet.newInstance()
                sheet.callback = object : StyleBottomSheet.Callback {
                    override fun onDone(selected: StyleItem?) {
                        selected?.let {
                            // apply style or update UI
                            setupRecycler()
                        }
                    }

                    override fun onCancel() {
//                        Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
                sheet.show(parentFragmentManager, "StyleBottomSheet")

            }
        }

    }
    fun View.showKeyboard() {
        requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
    private fun showCustomPopupVariations(view: View) {
        // Inflate the custom layout for the popup
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.popup_custom_layout, null)

        // Convert dp to px
        fun dp(value: Int) = (value * requireContext().resources.displayMetrics.density).toInt()

        // Create the PopupWindow with width matching the anchor view
        val popupWindow = PopupWindow(
            popupView,
            view.width, // match width of the anchor view
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            isOutsideTouchable = true
            isFocusable = true
            setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_round)) // optional background
        }

        // Show the popup window below the anchor view with a little vertical margin
        val margin = dp(8) // 8dp margin
        popupWindow.showAsDropDown(view, 0, margin)

        // Get the option views
        val img1 = popupView.findViewById<TextView>(R.id.image1)
        val img2 = popupView.findViewById<TextView>(R.id.image2)
        val img3 = popupView.findViewById<TextView>(R.id.image3)
        val img4 = popupView.findViewById<TextView>(R.id.image4)

        // Highlight selected item
        val highlight = { tv: TextView ->
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorprimary))
            tv.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_round)
        }
        when (selectedItemIdPosition) {
            0 -> highlight(img1)
            1 -> highlight(img2)
            2 -> highlight(img3)
            3 -> highlight(img4)
        }

        // Handle clicks
        val updateSelection: (Int, String) -> Unit = { pos, text ->
            selectedItemIdPosition = pos
            binding?.btnVariations?.text = text
            popupWindow.dismiss()
        }

        img1.setOnClickListener { updateSelection(0, getString(R.string._01_image)) }
        img2.setOnClickListener { updateSelection(1, getString(R.string._02_images)) }
        img3.setOnClickListener { updateSelection(2, getString(R.string._03_images)) }
        img4.setOnClickListener { updateSelection(3, getString(R.string._04_images)) }
    }



    private fun showCustomPopupCanvas(view: View, margin: Int = 8) { // margin in dp
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.popup_custom_layout_canvas, null)

        // Create PopupWindow matching width of anchor view
        val popupWindow = PopupWindow(
            popupView,
            view.width, // match width
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            isOutsideTouchable = true
            isFocusable = true
            setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_round))
            elevation = mActivity?.dp(8)?.toFloat()?:8f // requires API 21+
        }

        // Convert margin from dp to px
        val marginPx = mActivity?.dp(margin)

        // Show popup with margin from anchor view
        marginPx?.let { marginPx->
            popupWindow.showAsDropDown(view, 0, marginPx)
        }

        // Option views
        val square = popupView.findViewById<TextView>(R.id.square)
        val portrait = popupView.findViewById<TextView>(R.id.portrait)
        val landscape = popupView.findViewById<TextView>(R.id.landscape)

        // Highlight selected item
        val highlight = { tv: TextView ->
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorprimary))
            tv.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_round)
        }
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.colorprimary)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.lightGrey)

        // Tint all items
        landscape.setDrawableTint(if (selectedItemIdPositionCanvas == 2) selectedColor else defaultColor)
        portrait.setDrawableTint(if (selectedItemIdPositionCanvas == 1) selectedColor else defaultColor)
        square.setDrawableTint(if (selectedItemIdPositionCanvas == 0) selectedColor else defaultColor)

        when (selectedItemIdPositionCanvas) {
            0 -> highlight(square)
            1 -> highlight(portrait)
            2 -> highlight(landscape)
        }

        // Handle clicks
        val updateSelection: (Int, TextView, String) -> Unit = { pos, tv, text ->
            selectedItemIdPositionCanvas = pos
            binding?.btnCanvas?.text = text

            highlight(tv)
            popupWindow.dismiss()
        }

        square.setOnClickListener { updateSelection(0, square, getString(R.string._1_1))
            binding?.btnCanvas?.setDrawableWithTint(R.drawable.square_svg, defaultColor)
        }
        portrait.setOnClickListener { updateSelection(1, portrait, getString(R.string._4_5))
            binding?.btnCanvas?.setDrawableWithTint(R.drawable.portrait_svg, defaultColor)}
        landscape.setOnClickListener { updateSelection(2, landscape, getString(R.string._4_3))
            binding?.btnCanvas?.setDrawableWithTint(R.drawable.landscape_svg, defaultColor)}
    }


}


