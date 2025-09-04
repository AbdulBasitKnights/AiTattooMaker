package com.basit.aitattoomaker.presentation.setting

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.FragmentSettingsBinding
import com.basit.aitattoomaker.extension.openLink
import com.basit.aitattoomaker.extension.openRateUs
import com.basit.aitattoomaker.extension.shareAppLink
import com.basit.aitattoomaker.extension.shareAppLinkTo
import com.basit.aitattoomaker.extension.showExitDialog
import com.basit.aitattoomaker.presentation.setting.adapter.ModelSettings
import com.basit.aitattoomaker.presentation.setting.adapter.SettingsAdapter
import com.basit.aitattoomaker.presentation.setting.adapter.SettingsClickListener
import com.basit.aitattoomaker.presentation.utils.AppUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
@AndroidEntryPoint
class SettingFragment : Fragment(), SettingsClickListener {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var settingsAdapter: SettingsAdapter
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
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
            mActivity?.let {
                AppUtils.getMain(it)?.hidebottombar()
            }
        mActivity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            mActivity?.showExitDialog(
                onDiscard = {
                    // User clicked discard, handle accordingly
                            findNavController().popBackStack(R.id.navigation_aicamera,false)
                },
                onNotNow = {
                    // User clicked not now, just dismiss dialog
                }
            )
        }
        binding?.backActionbar?.setOnClickListener {
            findNavController().popBackStack(R.id.navigation_aicamera,false)
        }
    }

    private fun setupRecycler() {
        mActivity?.let { activity ->
//            if (GlobalValues.isProVersion) {
//                binding?.banner?.hide()
//            }
            settingsAdapter = SettingsAdapter(activity, arrayListOf(), this)
            val layoutManager = LinearLayoutManager(activity)
            binding?.rvSettings?.layoutManager = layoutManager
            binding?.rvSettings?.adapter = settingsAdapter
            lifecycleScope.launch {
                getSettingsFlow().collectLatest { list ->
                    withContext(Dispatchers.Main) {
                        settingsAdapter?.updateList(list)

                    }
                }
            }
        }
    }
    private fun getSettingsFlow(): Flow<List<ModelSettings>> = flow {
        val list = arrayListOf<ModelSettings>()
        list.add(ModelSettings("General", true))
        list.add(
            ModelSettings(
                "Privacy Policy",
                false,
                icon = R.drawable.privacy_svg,
                background = getDrawable(mActivity?:requireActivity(),R.drawable.bg_top_round_grey)
            )
        )
        list.add(
            ModelSettings(
                "Terms & Conditions",
                false,
                icon = R.drawable.terms_svg,
                background = getDrawable(mActivity?:requireActivity(),R.drawable.bg_grey),
                showView = true
            )
        )
        list.add(
            ModelSettings(
                "Rate Us",
                false,
                icon = R.drawable.rateus_svg,
                background = getDrawable(mActivity?:requireActivity(),R.drawable.bg_bottom_round_grey),
                showView = false
            )
        )

        list.add(ModelSettings("Social", true))
        list.add(
            ModelSettings(
                "Share App",
                false,
                icon = R.drawable.shared_svg,
                background = getDrawable(mActivity?:requireActivity(),R.drawable.bg_top_round_grey)
            )
        )
        list.add(
            ModelSettings(
                "Instagram",
                false,
                icon = R.drawable.ig_svg,
                background = getDrawable(mActivity?:requireActivity(),R.drawable.bg_grey)
            )
        )
        list.add(
            ModelSettings(
                "Facebook",
                false,
                icon = R.drawable.fb_svg,
                background = getDrawable(mActivity?:requireActivity(),R.drawable.bg_grey)
            )
        )
        list.add(
            ModelSettings(
                "Twitter",
                false,
                icon = R.drawable.x_svg,
                background = getDrawable(mActivity?:requireActivity(),R.drawable.bg_bottom_round_grey),
                showView = false
            )
        )
        emit(list)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSettingItemClick(which: String) {
        when(which){
            "Privacy Policy"->{
                mActivity?.openLink("https://www.terafort.com/privacy.html")
            }
            "Share App"->{
                mActivity?.shareAppLink()
            }
            "Rate Us"->{
                mActivity?.openRateUs()
            }
            "Terms & Conditions"->{
                mActivity?.openLink("https://www.terafort.com/T&C.html")
            }

            "Twitter" -> {
                mActivity?.shareAppLinkTo("twitter")
            }
            "Instagram" -> {
                mActivity?.shareAppLinkTo("instagram")
            }
            "Facebook" -> {
                mActivity?.shareAppLinkTo("facebook")
            }

        }

    }
}