package com.basit.aitattoomaker.presentation.result

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.data.sealed.GenerationUiState
import com.basit.aitattoomaker.databinding.FragmentResultBinding
import com.basit.aitattoomaker.extension.showDiscardDialog
import com.basit.aitattoomaker.presentation.MainViewModel
import com.basit.aitattoomaker.presentation.result.adapter.ResultAdapter
import com.basit.aitattoomaker.presentation.result.model.ResultItem
import com.basit.aitattoomaker.presentation.utils.AppUtils
import com.basit.aitattoomaker.presentation.utils.DialogUtils.creationDialog
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val viewModel: MainViewModel by activityViewModels()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var adapter: ResultAdapter
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
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity->
            buttonClicks()
            AppUtils.getMain(activity)?.hidebottombar()
            mActivity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
                mActivity?.showDiscardDialog(
                    onDiscard = {
                        // User clicked discard, handle accordingly
                        findNavController().popBackStack(R.id.navigation_aicreate,false)
                    },
                    onNotNow = {
                        // User clicked not now, just dismiss dialog
                    }
                )
            }
            imageGenerationsObserver()
            recyclerAdapter()

        }
    }

    private fun recyclerAdapter() {
        mActivity?.let { activity ->
            binding?.apply {
                adapter = ResultAdapter { selected ->
                    Glide.with(activity)
                        .load(selected.imageUrl)
                        .into(binding.currentImage)
                }
                rvImageVariations.adapter = adapter
                adapter.submitList(emptyList())
            }
        }

    }

    private fun imageGenerationsObserver() {
        mActivity?.let { activity->
            viewModel.state.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is GenerationUiState.Idle -> {
                        // Nothing yet
                    }
                    is GenerationUiState.Loading -> {
                        creationDialog?.show()
                    }
                    is GenerationUiState.Success -> {
                        creationDialog?.dismiss()
                        val data = state.data.response
                        Glide.with(activity)
                            .load(data.generated_img_url)
                            .into(binding.currentImage)
                        adapter.submitList(listOf(ResultItem(imageUrl = data.generated_img_url)))
                        Toast.makeText(context, "Generated: ${data.generated_img_url}", Toast.LENGTH_SHORT).show()
                    }
                    is GenerationUiState.Error -> {
                        creationDialog?.dismiss()
                        Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }
    private fun buttonClicks() {
        binding?.apply {
            regenerate?.setOnClickListener {
//                    findNavController().navigate(HomeFragmentDirections.actionNavigationHomeToNavigationAicreate())
            }
            tryOn?.setOnClickListener {
                Toast.makeText(activity, "Upcoming Feature", Toast.LENGTH_SHORT).show()
            }
            share?.setOnClickListener {  }
            download?.setOnClickListener {  }
            backPress?.setOnClickListener {
                try {
                    mActivity?.showDiscardDialog(
                        onDiscard = {
                            // User clicked discard, handle accordingly
                            findNavController().popBackStack(R.id.navigation_aicreate,false)
                        },
                        onNotNow = {
                            // User clicked not now, just dismiss dialog
                        }
                    )
                }
                catch (e:Exception){
                    e.printStackTrace()
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}