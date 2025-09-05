package com.basit.aitattoomaker.presentation.iap

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.ads.AdsManager.isPremiumSubscription
import com.basit.aitattoomaker.data.repo.NetworkUtils
import com.basit.aitattoomaker.data.repo.SubscriptionPlan
import com.basit.aitattoomaker.databinding.FragmentProPanelBinding
import com.basit.aitattoomaker.extension.activate
import com.basit.aitattoomaker.extension.deactivate
import com.basit.aitattoomaker.extension.fadesIn
import com.basit.aitattoomaker.extension.hide
import com.basit.aitattoomaker.extension.openLink
import com.basit.aitattoomaker.extension.popBackStack
import com.basit.aitattoomaker.presentation.MainViewModel
import com.basit.aitattoomaker.presentation.iap.IapManager.skuKeyMonthly
import com.basit.aitattoomaker.presentation.iap.IapManager.skuKeyWeekly
import com.basit.aitattoomaker.presentation.iap.adapter.RewardAdapter
import com.basit.aitattoomaker.presentation.iap.inapppurchases.BillingClientConnectionListener
import com.basit.aitattoomaker.presentation.iap.inapppurchases.DataWrappers
import com.basit.aitattoomaker.presentation.iap.inapppurchases.DataWrappers.*
import com.basit.aitattoomaker.presentation.iap.inapppurchases.IapConnector
import com.basit.aitattoomaker.presentation.iap.inapppurchases.SubscriptionServiceListener
import com.basit.aitattoomaker.presentation.utils.access_Token
import com.bumptech.glide.Glide
import com.singular.sdk.Singular
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.getValue

@AndroidEntryPoint
class ProPanelFragment : Fragment() {
    private var onCloseListener: OnCloseListener? = null
    var binding: FragmentProPanelBinding? = null
    private val viewModel: MainViewModel by activityViewModels()
    private var mActivity: FragmentActivity? = null
    var subPlans: List<SubscriptionPlan>? = null
    private lateinit var iapConnector: IapConnector
    private val skuDetailsMap = mutableMapOf<String, ProductDetails>()
    var weekPrice: Double? = 0.0
    var monthPrice: Double? = 0.0
    var currency: String? = ""
    var weekInfo: ProductDetails? = null
    var monthInfo: ProductDetails? = null

    interface OnCloseListener {
        fun onCloseCalledFromFragmentB()
    }

    fun setOnCloseListener(listener: OnCloseListener) {
        this.onCloseListener = listener
    }

    private var priceModel: MutableList<CustomInAppModel>? = null
    var iapDetail: DataWrappers.ProductDetails? = null
    val items = listOf(
        RewardItem(R.drawable.ic_blue_check, "5600 Credits Monthly", "5600/Month"),
        RewardItem(R.drawable.ic_blue_check, "No Watermark", "Yes"),
        RewardItem(R.drawable.ic_blue_check, "Image Variations", "Yes"),
        RewardItem(R.drawable.ic_blue_check, "Ad-Free Experience", "Yes"),
        RewardItem(R.drawable.ic_blue_check, "Art Styles", "Unlimited"),
        RewardItem(R.drawable.empty, "", "")
    )
    private val adapter by lazy {
        RewardAdapter(
            items
        )
    }
    private var currentPlan = "monthly"
    private val handler = Handler(Looper.getMainLooper())

    private val imageSwitcher = object : Runnable {
        override fun run() {
            binding?.viewPager?.let { viewPager ->
                val nextPage = (viewPager.currentItem + 1)
                viewPager.setCurrentItem(nextPage, true)
                handler.postDelayed(this, 2500)
            }


        }
    }
    val isBillingClientConnected: MutableLiveData<Boolean> = MutableLiveData()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProPanelBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let {
            setRewardAdapter()
            viewModel.getSubPlan()
            observer()
            /*binding?.videoView?.let { it1 ->
                Glide.with(it)
                    .load(R.drawable.secondinapp_video)
                    .into(it1)
            }*/
            lifecycleScope.launch {
                delay(3000)
                withContext(Dispatchers.Main) {
                    binding?.ivClose?.fadesIn()
                }
            }
            binding?.tvterms?.setOnClickListener {
                mActivity?.openLink("")
            }
            binding?.tvPrivacy?.setOnClickListener {
                mActivity?.openLink("")
            }
            //  showGoogleSignInPanel()
            binding?.monthlyButton?.activate()
            binding?.btnPayment?.deactivate()
            if (!NetworkUtils.isOnline(it)) {
                Toast.makeText(
                    it,
                    "No Network Connection, Please Try Again\"",
                    Toast.LENGTH_SHORT
                ).show()
            }
            setClickListners()
            isBillingClientConnected.value = false

        }
    }

    private fun setRewardAdapter() {
        binding?.recyclerList?.adapter = adapter
    }

    private fun observer() {
        viewModel?.getPlan?.observe(viewLifecycleOwner) {
            if (it != null) {
                subPlans = it
                mActivity?.let {
                    iapConnector = IapManager.getIapConnector(it)
                    initInAppListeners()
                }
            }
        }
    }

    private fun setClickListners() {
        binding?.apply {
            ivClose.setOnClickListener {
                onCloseListener?.onCloseCalledFromFragmentB()
                parentFragmentManager.setFragmentResult("fragmentB_close", Bundle())
                try {
                    findNavController().popBackStack()
                } catch (e: Exception) {
                    mActivity?.popBackStack()
                }
            }
            layoutMonthly.setOnClickListener {
                currentPlan = "monthly"
                it.activate()
                mActivity?.let { activity ->
                    items?.get(0)?.title = "5600 Credits Monthly"
                    adapter?.notifyItemChanged(0)


                    priceModel?.get(1)?.let { it1 ->
                        setUpMonthlyUi(it1)
                    }


                }
                layoutMonthly?.background =
                    mActivity?.resources?.getDrawable(R.drawable.bg_selector_selected)
                layoutWeekly?.background =
                    mActivity?.resources?.getDrawable(R.drawable.bg_selector_unselected)
                ivMonthlyRadio?.background =
                    mActivity?.resources?.getDrawable(R.drawable.new_check_selector)
                ivWeeklyRadio?.background =
                    mActivity?.resources?.getDrawable(R.drawable.new_uncheck)
                layoutWeekly.deactivate()
            }
            layoutWeekly.setOnClickListener {
                currentPlan = "weekly"
                mActivity?.let { activity ->
                    items?.get(0)?.title = "1400 Credits Weekly"
                    adapter?.notifyItemChanged(0)
//                    adapter?.notifyDataSetChanged()
                    priceModel?.get(0)?.let { it1 -> setUpWeeklyUi(it1) }
                }
                it.activate()
                layoutMonthly?.background =
                    mActivity?.resources?.getDrawable(R.drawable.bg_selector_unselected)
                layoutWeekly?.background =
                    mActivity?.resources?.getDrawable(R.drawable.bg_selector_selected)
                ivMonthlyRadio?.background =
                    mActivity?.resources?.getDrawable(R.drawable.new_uncheck)
                ivWeeklyRadio?.background =
                    mActivity?.resources?.getDrawable(R.drawable.new_check_selector)
                layoutMonthly.deactivate()
            }

            binding?.btnPayment?.setOnClickListener {
                if (isPremiumSubscription.value != true) {
                    mActivity?.let {
                          if (currentPlan == "monthly") {
                              if(access_Token!=null){
                                  iapConnector.subscribe(it, skuKeyMonthly)
                              }
                              else{
                                  Toast.makeText(requireContext(), "Please wait...", Toast.LENGTH_SHORT).show()
                              }
                          } else {
                              if(access_Token!=null) {
                                  iapConnector.subscribe(it, skuKeyWeekly)
                              }
                              else{
                                  Toast.makeText(requireContext(), "Please wait...", Toast.LENGTH_SHORT).show()
                              }
                          }

                    }
                } else {
                    Toast.makeText(mActivity, "You are already subscribed", Toast.LENGTH_SHORT)
                        .show()
                }

            }
        }
    }

    private fun initInAppListeners() {
        iapConnector.addBillingClientConnectionListener(object : BillingClientConnectionListener {
            override fun onConnected(status: Boolean, billingResponseCode: Int) {
                isBillingClientConnected.value = status
            }
        })

        iapConnector.addSubscriptionListener(object : SubscriptionServiceListener {
            override fun onSubscriptionRestored(purchaseInfo: PurchaseInfo) {
                // will be triggered upon fetching owned subscription upon initialization
                Log.e("checkPricesDetails", "Info: $purchaseInfo")
                when (purchaseInfo.sku) {
                    skuKeyWeekly -> {
//                        lifecycleScope.launch {
//                            viewModel.purchaseSubscription(1,"","0.0")
//                        }
                        isPremiumSubscription.postValue(true)
                    }

                    skuKeyMonthly -> {
//                        lifecycleScope.launch {
//                            viewModel.purchaseSubscription(2,"","0.0")
//                        }
                        isPremiumSubscription.postValue(true)
                    }

                    else -> {
                        isPremiumSubscription.value = false
                    }
                }
            }

            override fun onSubscriptionPurchased(purchaseInfo: PurchaseInfo) {
                // will be triggered whenever subscription succeeded
                when (purchaseInfo.sku) {
                    skuKeyWeekly -> {
                        isPremiumSubscription.postValue(true)
                        mActivity?.let {
                            viewModel.purchaseSubscription(1,"","0.0")
                            weekPrice?.let { amount ->
                                sendPurchaseToSingular(
                                    purchaseInfo.originalJson,
                                    purchaseInfo.signature,
                                    amount
                                )
                            }

                        }
                        weekPrice?.let {
                            Singular.revenue(currency, it, weekInfo)
                        }
                    }
                    skuKeyMonthly -> {
                        isPremiumSubscription.postValue(true)
                        mActivity?.let {
                            viewModel.purchaseSubscription(1,"","0.0")
                        }
                        monthPrice?.let { amount ->
                            sendPurchaseToSingular(
                                purchaseInfo.originalJson,
                                purchaseInfo.signature,
                                amount
                            )
                        }
                    }

                    else -> {
                        isPremiumSubscription.value = false
                        mActivity?.popBackStack()
                    }
                }

            }

            override fun onPricesUpdated(iapKeyPrices: Map<String, List<ProductDetails>>) {
                Log.e("checkNewValues", "$iapKeyPrices")

                val pricesList = MutableList<ProductDetails?>(3) { null }

                for ((_, productDetails) in iapKeyPrices) {
                    for (productDetail in productDetails) {
                        val title = productDetail.title?.lowercase(Locale.ROOT).orEmpty()

                        when {
                            "weekly" in title -> {
                                weekPrice = productDetail.priceAmount?.toDouble()
                                weekInfo = productDetail
                                currency = productDetail.priceCurrencyCode
                                pricesList[0] = productDetail

                                Log.w(
                                    "checkSubscription",
                                    "Subs Week Offer: ${productDetail.offerPercentage ?: 0}% OFF"
                                )
                                Log.w(
                                    "checkSubscription",
                                    "Subs Week Description: ${productDetail.description}"
                                )
                            }

                            "offer" in title -> {
                                monthPrice = productDetail.priceAmount?.toDouble()
                                monthInfo = productDetail
                                currency = productDetail.priceCurrencyCode
                                pricesList[2] = productDetail

                                Log.w(
                                    "checkSubscription",
                                    "Subs Month Offer: ${productDetail.offerPercentage ?: 0}% OFF"
                                )
                                Log.w(
                                    "checkSubscription",
                                    "Subs Month Description: ${productDetail.description}"
                                )
                            }

                            "monthly" in title -> {
                                monthPrice = productDetail.priceAmount?.toDouble()
                                monthInfo = productDetail
                                currency = productDetail.priceCurrencyCode
                                pricesList[1] = productDetail

                                Log.w(
                                    "checkSubscription",
                                    "Subs Month Offer: ${productDetail.offerPercentage ?: 0}% OFF"
                                )
                                Log.w(
                                    "checkSubscription",
                                    "Subs Month Description: ${productDetail.description}"
                                )
                            }

                            else -> {
                                // Optional fallback
                                pricesList[0] = productDetail
                                weekInfo = productDetail
                                weekPrice = productDetail.priceAmount?.toDouble()
                            }
                        }
                    }
                }

                if (pricesList[0]?.price != null && pricesList[1]?.price != null) {
                    subscribeUi(pricesList.filterNotNull())
                }
            }


        })

        isBillingClientConnected.observe(viewLifecycleOwner) { connected ->
            when (connected) {
                true -> {
                    //selectContinue()
                }

                else -> {
                    // unSelectContinue()
                }
            }
        }
    }

    fun sendPurchaseToSingular(
        originalJson: String,
        signature: String,
        amount: Double
    ) {
        Singular.revenue(
            currency,
            amount,
            originalJson,
            signature
        )
    }
    override fun onResume() {
        super.onResume()
        if(subPlans!=null){
            initInAppListeners()
        }
    }
    private fun subscribeUi(product_list: List<ProductDetails>?) {
        product_list?.let { list ->
            try {
                binding?.textFetchingPrices?.hide()
                val rule = list[0].priceAmount?.times(4)
                val totalMonthly = "${list[0].priceCurrencyCode} $rule"
                val discounted_weekly =
                    "${list[0].priceCurrencyCode} ${list[1].priceAmount?.div(4)}"
                val tempPriceList = mutableListOf<CustomInAppModel>()
                val actualPrice = list[1].priceAmount
                val percent =
                    ((rule?.let { it.minus(actualPrice ?: 0.0) })?.div(rule))?.times(100)?.toInt()
//                Log.e("checkPrice","actual price:$actualPrice\nrule:$rule")
                tempPriceList.add(
                    0,
                    CustomInAppModel(
                        0,
                        "Weekly",
                        "",
                        list[0].price.toString(),
                        list[0].price.toString(),
                        "",
                        false
                    )
                )
                rule?.let {
                    percent?.toString()?.let { it1 ->
                        list[1].description?.let { it2 ->
                            CustomInAppModel(
                                id = 1,
                                durationPlan = "Monthly",
                                description = it2,
                                totalPrice = totalMonthly,
                                discountedPrice = list[1].price.toString(),
                                discountPercent = it1 + " %OFF",
                                showDiscount = false,
                                monthlyPrice = it,
                                pricePerWeek = discounted_weekly
                            )
                        }
                    }
                }?.let {
                    tempPriceList.add(
                        1,
                        it
                    )
                }
                rule?.let {
                    percent?.toString()?.let { it1 ->
                        list[2].description?.let { it2 ->
                            CustomInAppModel(
                                id = 2,
                                durationPlan = "Offer",
                                description = it2,
                                totalPrice = totalMonthly,
                                discountedPrice = list[2].price.toString(),
                                discountPercent = it1 + " %OFF",
                                showDiscount = false,
                                monthlyPrice = it,
                                pricePerWeek = discounted_weekly
                            )
                        }
                    }
                }?.let {
                    tempPriceList.add(
                        2,
                        it
                    )
                }


                rule?.let {
                    percent?.toString()?.let { it1 ->
                        list[2].description?.let { it2 ->
                            CustomInAppModel(
                                id = 2,
                                durationPlan = "MonthlyOffer",
                                description = it2,
                                totalPrice = totalMonthly,
                                discountedPrice = list[2].price.toString(),
                                discountPercent = it1 + " %OFF",
                                showDiscount = false,
                                monthlyPrice = it,
                                pricePerWeek = discounted_weekly
                            )
                        }
                    }
                }?.let {
                    tempPriceList.add(
                        2,
                        it
                    )
                }
                priceModel = tempPriceList
                setUpMonthlyUi(tempPriceList[1])
                priceModel?.get(0)?.let { it1 -> setUpWeeklyUi(it1) }
            } catch (e: Exception) {
                Log.e("checkInApp", "subscribeUi : $e")
            }


        }

    }

    private fun setUpMonthlyUi(inAppModel: CustomInAppModel) {
        binding?.apply {
            try {
                binding?.btnPayment?.activate()
                binding?.btnPayment?.background=resources.getDrawable(R.drawable.gradient)
//                binding?.tvBestDeals?.text=inAppModel.discountPercent
                val price = inAppModel.monthlyPrice
                val discountedPrice = "${price.toString()}0"
                val spannableString = SpannableString(discountedPrice)
                spannableString.setSpan(
                    StrikethroughSpan(),
                    0,
                    spannableString.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                // Use SpannableStringBuilder to concatenate formatted text
                val result = inAppModel.pricePerWeek
                val finalText = SpannableStringBuilder()
                    .append("Just ${inAppModel.discountedPrice} per month ")
                    .append(spannableString) // Append the strikethrough price

                tvMonthlySubtitle.text = finalText
                tvMonthlyPrice.text = "$result"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setUpWeeklyUi(inAppModel: CustomInAppModel) {
        binding?.apply {
            binding?.btnPayment?.activate()
            binding?.btnPayment?.background=resources.getDrawable(R.drawable.gradient)
//            tvMonthly.text = inAppModel.durationPlan
            val priceweek = "${inAppModel.totalPrice}"
            tvWeeklyPrice.text = "$priceweek"
        }
    }

    fun getPriceUnit(input: String): Pair<String, String>? {
        // Check if the input string contains a digit
        val digitIndex = input.indexOfFirst { it.isDigit() }

        return if (digitIndex != -1) {
            // Extracting the unit and price based on the digit index
            val unit = input.substring(0, digitIndex)
            val price = input.substring(digitIndex)

            Pair(unit, price)
        } else {
            // If no digit is found, return null
            null
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = requireActivity()
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }


    @SuppressLint("WrongConstant")
    override fun onDestroyView() {
        super.onDestroyView()
        mActivity?.let { activity ->
//            requireActivity().hideNavigationBar()
        }
    }

}