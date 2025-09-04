package com.basit.aitattoomaker.presentation.iap

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.basit.aitattoomaker.databinding.FragmentProPanelBinding


class ProPanelFragment : Fragment() {
    private var onCloseListener: OnCloseListener? = null
    var binding: FragmentProPanelBinding? = null
    private var mActivity: FragmentActivity? = null
//    private lateinit var iapConnector: IapConnector
//    private val skuDetailsMap = mutableMapOf<String, ProductDetails>()
    var weekPrice:Double?=0.0
    var monthPrice:Double?=0.0
    var currency:String?=""
//    var weekInfo:ProductDetails?=null
//    var monthInfo:ProductDetails?=null
    interface OnCloseListener {
        fun onCloseCalledFromFragmentB()
    }
    fun setOnCloseListener(listener: OnCloseListener) {
        this.onCloseListener = listener
    }
    private var priceModel:  MutableList<CustomInAppModel>?=null
//    var iapDetail: ProductDetails?=null
//    val items = listOf(
//        RewardItem(R.drawable.ic_credits, "5600 Credits Monthly", "5600/Month"),
//        RewardItem(R.drawable.sparkle, "Access to AI Videos", "Yes"),
//        RewardItem(R.drawable.ic_remove_watermark, "No Watermark", "Yes"),
//        RewardItem(R.drawable.ic_ai_models, "Premium AI Reels", "All"),
//        RewardItem(R.drawable.ic_hide_prompts, "Hide Prompt", "Yes"),
//        RewardItem(R.drawable.ic_try_prompts, "Try others Prompts", "Unlimited"),
//        RewardItem(R.drawable.ic_fast_creations, "Fast Creations", "Yes"),
//        RewardItem(R.drawable.ic_image_variations, "Image Variations", "Yes"),
//        RewardItem(R.drawable.ic_ads_free, "Ad-Free Experience", "Yes"),
//        RewardItem(R.drawable.ic_art_styles, "Art Styles", "Unlimited"),
//        RewardItem(R.drawable.empty, "", "")
//    )
//    private val adapter by lazy {
//        RewardAdapter(
//           items
//        )
//    }
    private var currentPlan = "monthly"
//    private val imageArray = arrayListOf(
//        R.drawable.propanel_first,
//        R.drawable.propanel_second,
//        R.drawable.propanel_third
//    )
//    private val handler = Handler(Looper.getMainLooper())

  /*  private val imageSwitcher = object : Runnable {
        override fun run() {
            binding?.viewPager?.let { viewPager ->
                val nextPage = (viewPager.currentItem + 1)
                viewPager.setCurrentItem(nextPage, true)
                handler.postDelayed(this, 2500)
            }


        }
    }*/
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
       /* mActivity?.let {
            FirebaseEvents.firebaseUserActionNew("IAPP", "iap_view")
            FirebaseEvents.firebaseUserActionNew("IAPP", "pro_screen")
            firebaseUserActionScreenView("SecondInAppFragment","SecondInAppFragment")
            setRewardAdapter()
            observer()
            binding?.videoView?.let { it1 ->
                Glide.with(it)
                    .load(R.drawable.secondinapp_video)
                    .into(it1)
            }
            lifecycleScope.launch {
                delay(3000)
                withContext(Dispatchers.Main) {
                    binding?.ivClose?.fadesIn()
                }
            }
            binding?.tvterms?.setOnClickListener {
                mActivity?.openLink(Constants.TERMS_URL)
            }
            binding?.tvPrivacy?.setOnClickListener {
                mActivity?.openLink(Constants.PRIVACY_POLICY_URL)
            }

            //  showGoogleSignInPanel()
            binding?.monthlyButton?.activate()

            setClickListners()
            isBillingClientConnected.value = false
            iapConnector = IapManager.getIapConnector(it)
            if (!NetworkUtils.isOnline(it)) {
                Toast.makeText(
                    it,
                    "No Network Connection, Please Try Again\"",
                    Toast.LENGTH_SHORT
                ).show()
            }
            initInAppListeners()

        }*/
    }
   /* fun firebaseUserActionScreenView(activityName: String, actionName: String) {
        val action = formatString(actionName)

        Aspire.Companion.context?.let { ctx ->
            CoroutineScope(Dispatchers.IO).launch {
                if (FirebaseApp.getApps(ctx).isEmpty()) {
                    FirebaseApp.initializeApp(ctx)
                }

                val analytics = firebaseAnalytics ?: FirebaseAnalytics.getInstance(ctx).also {
                    firebaseAnalytics = it
                }

                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, activityName)
                    putString(FirebaseAnalytics.Param.SCREEN_CLASS, this@ProPanelFragment::class.java.simpleName)
                }

                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)

                Singular.event(action)
            }
        }
    }*/
    private fun setRewardAdapter() {
//        binding?.recyclerList?.adapter = adapter
    }

  /*  private fun observer() {
        creditsViewModel.subscription.observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        AppConstants.isPremiumSubscription.postValue(true)
                        getMain(mActivity)?.loadingScreenVisibilityHide()
                        val msg=" You've successfully subscribed\n" + resources.getString(R.string.app_name)
                        mActivity?.let { it1 -> paymentSucessDialogue(it1,msg) }
                        mActivity?.popBackStack()
                        creditsViewModel._subscription.postValue(null)
                        creditsViewModel.fetchProfilebyID()
    //                    Toast.makeText(mActivity, "Purchasing value credited", Toast.LENGTH_SHORT).show()
                    }

                    Status.ERROR -> {
                        getMain(mActivity)?.loadingScreenVisibilityHide()
                        val msg="You've successfully subscribed\n" + resources.getString(R.string.app_name)
                        mActivity?.let { it1 -> paymentErrorDialogue(it1) }
                        mActivity?.popBackStack()
    //                    Toast.makeText(mActivity, "${it.message}", Toast.LENGTH_SHORT).show()

                    }

                    Status.LOADING -> {
                        getMain(mActivity)?.loadingScreenVisibilityShow()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }*/

    /*private fun setClickListners() {
        binding?.apply {
            ivClose.setOnClickListener {
                onCloseListener?.onCloseCalledFromFragmentB()
                parentFragmentManager.setFragmentResult("fragmentB_close", Bundle())
                try {
                    findNavController().popBackStack()
                } catch (e: Exception) {
                    mActivity?.popBackStack()
                }
//                AdManager.loadInterstitialAd(requireActivity())
                FirebaseEvents.firebaseUserActionNew("ProPanel", "iap_cross_button_click")
            }
            layoutMonthly.setOnClickListener {
                currentPlan = "monthly"
                it.activate()
                mActivity?.let {activity->
                    items?.get(0)?.title="5600 Credits Monthly"
                    adapter?.notifyItemChanged(0)
                  *//*  priceModel?.get(1)?.let { it1 ->
                        setUpMonthlyUi(it1)
                     }*//*
                }
                layoutMonthly?.background=mActivity?.resources?.getDrawable(R.drawable.bg_selector_selected)
                layoutWeekly?.background=mActivity?.resources?.getDrawable(R.drawable.bg_selector_unselected)
                ivMonthlyRadio?.background=mActivity?.resources?.getDrawable(R.drawable.new_check_selector)
                ivWeeklyRadio?.background=mActivity?.resources?.getDrawable(R.drawable.new_uncheck)
                layoutWeekly.deactivate()
            }
            layoutWeekly.setOnClickListener {
                currentPlan = "weekly"
                mActivity?.let {activity->
                    items?.get(0)?.title="1400 Credits Weekly"
                    adapter?.notifyItemChanged(0)
//                    adapter?.notifyDataSetChanged()
                    priceModel?.get(0)?.let { it1 -> setUpWeeklyUi(it1) }
                }
                it.activate()
                layoutMonthly?.background=mActivity?.resources?.getDrawable(R.drawable.bg_selector_unselected)
                layoutWeekly?.background=mActivity?.resources?.getDrawable(R.drawable.bg_selector_selected)
                ivMonthlyRadio?.background=mActivity?.resources?.getDrawable(R.drawable.new_uncheck)
                ivWeeklyRadio?.background=mActivity?.resources?.getDrawable(R.drawable.new_check_selector)
                layoutMonthly.deactivate()
            }

            binding?.btnPayment?.setOnClickListener {
                if(AppConstants.isPremiumSubscription.value!= true){
                    mActivity?.let {
                        FirebaseEvents.firebaseUserActionNew("IAPP", "iap_btn_click")
                        if (currentPlan == "monthly") {
                            if(userViewModel.getUserLoggedIn()){
                                iapConnector.subscribe(it, skuKeyMonthly)
                            }
                            else{
                                Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if(userViewModel.getUserLoggedIn()) {
                                iapConnector.subscribe(it, skuKeyWeekly)
                            }
                            else{
                                Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }
                }
                else{
                    Toast.makeText(mActivity, "You are already subscribed", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }*/

/*    private fun initInAppListeners() {
        iapConnector.addBillingClientConnectionListener(object : BillingClientConnectionListener {
            override fun onConnected(status: Boolean, billingResponseCode: Int) {
                isBillingClientConnected.value = status
            }
        })

        iapConnector.addSubscriptionListener(object : SubscriptionServiceListener {
            override fun onSubscriptionRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered upon fetching owned subscription upon initialization
                Log.e("checkPricesDetails","Info: $purchaseInfo")
                when (purchaseInfo.sku) {
                    skuKeyWeekly -> {
                      *//*  lifecycleScope.launch {
                            creditsViewModel.subscribe("google","${purchaseInfo.purchaseToken}","${purchaseInfo.sku}")
                        }*//*
                        GlobalValues.isProVersion.value = true
                        //    mInterstitialAd = null
                        rewardedAd = null
                    }

                    skuKeyMonthly -> {
                       *//* lifecycleScope.launch {
                            creditsViewModel.subscribe("google","${purchaseInfo.purchaseToken}","${purchaseInfo.sku}")
                        }*//*
                        GlobalValues.isProVersion.value = true
                        // mInterstitialAd = null
                        rewardedAd = null
                    }

                    else -> {
                        GlobalValues.isProVersion.value = false
                    }
                }
            }

            override fun onSubscriptionPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered whenever subscription succeeded
                when (purchaseInfo.sku) {
                    skuKeyWeekly -> {
                        GlobalValues.isProVersion.postValue(true)
                        // mInterstitialAd = null
                        rewardedAd = null

                        FirebaseEvents.firebaseUserActionNew("IAPP", "iap_successful")
                        FirebaseEvents.firebaseUserActionNew("IAPP", "iap_weekly_subscribed")
                        mActivity?.let {
//                            val msg=" You've successfully subscribed\n" + resources.getString(R.string.app_name) + "weekly Pro"
//                            paymentSucessDialogue(it,msg)
                            creditsViewModel.subscribe(
                                "android","${purchaseInfo.purchaseToken}","${purchaseInfo.sku}"
                            )
                            weekPrice?.let {
                                amount->
                                sendPurchaseToSingular(purchaseInfo.originalJson,purchaseInfo.signature,amount)
                            }

                        }
//                        mActivity?.popBackStack()
                        weekPrice?.let {
                            Singular.revenue(currency,it,weekInfo)
                        }
                    }

                    skuKeyMonthly -> {

                        FirebaseEvents.firebaseUserActionNew("IAPP", "iap_successful")
                        FirebaseEvents.firebaseUserActionNew("IAPP", "iap_monthly_subscribed")
                        GlobalValues.isProVersion.postValue(true)
                        //  mInterstitialAd = null
                        rewardedAd = null
                        mActivity?.let {
                            creditsViewModel.subscribe(
                                "android","${purchaseInfo.purchaseToken}","${purchaseInfo.sku}"
                            )
//                            val msg="You've successfully subscribed\n" + resources.getString(R.string.app_name) + "monthly Pro"
//                            paymentSucessDialogue(it,msg)
//                            mActivity?.popBackStack()
                        }
//                        monthPrice?.let {
//                            Singular.revenue(currency,it,monthInfo)
//                        }
                        monthPrice?.let {
                                amount->
                            sendPurchaseToSingular(purchaseInfo.originalJson,purchaseInfo.signature,amount)
                        }
                    }
                    skuKeyMonthly -> {
                        FirebaseEvents.firebaseUserActionNew("IAPP", "iap_successful")
                        FirebaseEvents.firebaseUserActionNew("IAPP", "iap_monthly_subscribed")
                        GlobalValues.isProVersion.postValue(true)
                        //  mInterstitialAd = null
                        rewardedAd = null
                        mActivity?.let {
                            creditsViewModel.subscribe(
                                "android","${purchaseInfo.purchaseToken}","${purchaseInfo.sku}"
                            )
//                            val msg="You've successfully subscribed\n" + resources.getString(R.string.app_name) + "monthly Pro"
//                            paymentSucessDialogue(it,msg)
//                            mActivity?.popBackStack()
                        }
//                        monthPrice?.let {
//                            Singular.revenue(currency,it,monthInfo)
//                        }
                        monthPrice?.let {
                                amount->
                            sendPurchaseToSingular(purchaseInfo.originalJson,purchaseInfo.signature,amount)
                        }
                    }

                    else -> {
                        GlobalValues.isProVersion.value = false
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

                                Log.w("checkSubscription", "Subs Week Offer: ${productDetail.offerPercentage ?: 0}% OFF")
                                Log.w("checkSubscription", "Subs Week Description: ${productDetail.description}")
                            }

                            "offer" in title -> {
                                monthPrice = productDetail.priceAmount?.toDouble()
                                monthInfo = productDetail
                                currency = productDetail.priceCurrencyCode
                                pricesList[2] = productDetail

                                Log.w("checkSubscription", "Subs Month Offer: ${productDetail.offerPercentage ?: 0}% OFF")
                                Log.w("checkSubscription", "Subs Month Description: ${productDetail.description}")
                            }

                            "monthly" in title -> {
                                monthPrice = productDetail.priceAmount?.toDouble()
                                monthInfo = productDetail
                                currency = productDetail.priceCurrencyCode
                                pricesList[1] = productDetail

                                Log.w("checkSubscription", "Subs Month Offer: ${productDetail.offerPercentage ?: 0}% OFF")
                                Log.w("checkSubscription", "Subs Month Description: ${productDetail.description}")
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
    private fun subscribeUi(product_list: List<ProductDetails>?) {
        product_list?.let { list ->
            try {
                binding?.textFetchingPrices?.hide()
                val rule=list[0].priceAmount?.times(4)
                val totalMonthly="${list[0].priceCurrencyCode} $rule"
                val discounted_weekly="${list[0].priceCurrencyCode} ${list[1].priceAmount?.div(4)}"
                val tempPriceList = mutableListOf<CustomInAppModel>()
                val actualPrice=list[1].priceAmount
                val percent = ((rule?.let { it.minus(actualPrice ?: 0.0) })?.div(rule))?.times(100)?.toInt()
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
                                discountPercent = it1+" %OFF",
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
                                discountPercent = it1+" %OFF",
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
               *//* rule?.let {
                    percent?.toString()?.let { it1 ->
                        list[2].description?.let { it2 ->
                            CustomInAppModel(
                                id = 2,
                                durationPlan = "MonthlyOffer",
                                description = it2,
                                totalPrice = totalMonthly,
                                discountedPrice = list[2].price.toString(),
                                discountPercent = it1+" %OFF",
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
                }*//*
                priceModel=tempPriceList
//                setUpWeeklyUi(tempPriceList[0])
                    setUpMonthlyUi(tempPriceList[1])
//                setUpMonthlyOfferUi(tempPriceList[2])
                priceModel?.get(0)?.let { it1 -> setUpWeeklyUi(it1) }
            } catch (e: Exception) {
                Log.e("checkInApp", "subscribeUi : $e")
            }


        }

    }*/

    private fun setUpMonthlyUi(inAppModel: CustomInAppModel) {
        binding?.apply {
            try {
//                binding?.tvBestDeals?.text=inAppModel.discountPercent
                val price = inAppModel.monthlyPrice
                val discountedPrice = "${price.toString()}0"
                val spannableString = SpannableString(discountedPrice)
                spannableString.setSpan(StrikethroughSpan(), 0, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
//            tvMonthly.text = inAppModel.durationPlan
            val priceweek= "${inAppModel.totalPrice}"
            tvWeeklyPrice.text="$priceweek"
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