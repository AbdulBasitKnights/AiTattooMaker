package com.basit.aitattoomaker.presentation.result

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.basit.aitattoomaker.databinding.FragmentHomeBinding
import com.basit.aitattoomaker.databinding.FragmentResultBinding
import com.basit.aitattoomaker.extension.showDiscardDialog
import com.basit.aitattoomaker.presentation.home.HomeViewModel

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity->
            buttonClicks()
            mActivity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
                mActivity?.showDiscardDialog(
                    onDiscard = {
                        // User clicked discard, handle accordingly
                        findNavController().popBackStack() // example: go back
                    },
                    onNotNow = {
                        // User clicked not now, just dismiss dialog
                    }
                )
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
                            findNavController().popBackStack() // example: go back
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