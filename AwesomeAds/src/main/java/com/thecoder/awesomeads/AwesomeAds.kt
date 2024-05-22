package com.thecoder.awesomeads

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.thecoder.awesomeads.databinding.AdLoadingBinding
import com.thecoder.awesomeads.databinding.NativeViewBinding


import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

abstract class AwesomeAds:Application(),ActivityLifecycleCallbacks,LifecycleEventObserver {

  private  val hashMap=HashMap<String,NativeAd>()

    lateinit var act:Activity
   private lateinit var appOpenAd: AdmobAppOpen
    override fun onCreate() {
        super.onCreate()

        appOpenAd= AdmobAppOpen(this)
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        MobileAds.initialize(this)

    }


    fun loadAppOpen(showWhenloaded: Boolean,id:String,runnable: Runnable?){
        appOpenAd.preloadAd(showWhenloaded,id,runnable)
    }
    fun showAppOpen(runnable: Runnable?){
        appOpenAd.showAd(runnable)
    }

    fun showLoadNative(act: AppCompatActivity,id:String,frameLayout: FrameLayout,showMedia: Boolean){
        val native= AdmobNative(act)
        native.loadAndShowNative(frameLayout,id,showMedia)
    }


    fun preloadNative(id: String,uniqueKey: String){
        preloadNative(this,id,uniqueKey)
    }

    fun showPreloadedNative(uniqueKey: String,frameLayout: FrameLayout,showMedia: Boolean){
        val nativeAd= hashMap[uniqueKey]
        if(nativeAd!=null){


            frameLayout.visibility=View.VISIBLE
            frameLayout.removeAllViews()
            frameLayout.addView(inflateNativeView(nativeAd,NativeViewBinding.inflate(LayoutInflater.from(frameLayout.context)),showMedia))

        }
    }

    fun loadBanner(act: AppCompatActivity,id: String,frameLayout: FrameLayout,collapsable:Boolean=false,mrec: Boolean=false){
        val banner= AdmobBanner(act)
        banner.loadBanner(frameLayout,id,collapsable, mrec)

    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if(event== Lifecycle.Event.ON_START){
            appOpenAd.showAd()
        }

        if(event== Lifecycle.Event.ON_DESTROY){

            for(ad in hashMap.values){
                ad.destroy()
            }

            hashMap.clear()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        act=activity
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }





   private inner class AdmobAppOpen(app:Application){


        private val application=app
       private var isShowing=false

       lateinit var id2: String

        private  var isloading=false
        private   var openAd: AppOpenAd?=null
        fun preloadAd(showWhenloaded:Boolean,adId:String,runnable: Runnable?=null){
            if(isloading)return




            if(!this::id2.isInitialized){
                id2=adId
            }

            val id=if(BuildConfig.DEBUG){
                "ca-app-pub-3940256099942544/9257395921"
            }else{
                id2
            }

            isloading=true
            val request=AdRequest.Builder().build()

            AppOpenAd.load(application,id,request,object : AppOpenAd.AppOpenAdLoadCallback(){
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    isloading=false
                }

                override fun onAdLoaded(p0: AppOpenAd) {
                    super.onAdLoaded(p0)
                    openAd=p0

                    if(showWhenloaded){
                        showAd(runnable)
                    }

                }
            })
        }

        fun showAd(runnable: Runnable?=null){

            if(isShowing)return


            if(openAd!=null&&::act.isInitialized){
                isShowing=true
                openAd?.fullScreenContentCallback=object : FullScreenContentCallback(){

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        isloading=false
                        isShowing=false
                        openAd=null
                        preloadAd(false,id2)
                        runnable?.run()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        isloading=false
                        openAd=null
                        isShowing=false
                        runnable?.run()
                        preloadAd(false,id2)
                    }
                }
                openAd?.show(act)
            }else{
                runnable?.run()
            }
        }


    }





  private  fun preloadNative(context: Context,id2: String,uniqueKey:String){

        val native= hashMap[uniqueKey]
        if(native!=null){
          return
        }


        val id=if(BuildConfig.DEBUG){
            "ca-app-pub-3940256099942544/2247696110"
        }else{
            id2
        }
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()
        val nativeAdOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
        val adRequest = AdRequest.Builder().build()

        val listener=object :AdListener(){
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)


            }

            override fun onAdImpression() {
                super.onAdImpression()
//                val native2= hashMap[uniqueKey]
//                if(native2!=null){
//                    native2.destroy()
//                    hashMap.remove(uniqueKey)
//                }

            }

            override fun onAdLoaded() {
                super.onAdLoaded()



            }
        }
        val adLoader= AdLoader.Builder(context,id).forNativeAd {

            hashMap[uniqueKey]=it

        }.withNativeAdOptions(nativeAdOptions)
            .withAdListener(listener)

            .build()


        adLoader.loadAd(adRequest)
    }




   private class AdmobBanner(act: AppCompatActivity) : DefaultLifecycleObserver {
        private val activity=act

       init {
           activity.lifecycle.addObserver(this)
        }

        lateinit var adView: AdView
        var isLoading=false
        fun loadBanner(frameLayout: FrameLayout,id2: String,collapsable:Boolean,mrec:Boolean=false){

            if(isLoading)return


            frameLayout.visibility= View.VISIBLE
            val id=if(BuildConfig.DEBUG){
                "ca-app-pub-3940256099942544/9214589741"
            }else{
                id2
            }

            val banner= AdView(activity)
            if(mrec){

                banner.setAdSize(AdSize.MEDIUM_RECTANGLE)
            }else{
                val metrics= activity.resources.displayMetrics

                val densityAdjustment = if (metrics.density > 1) (1.0 / metrics.density) else 1.0
                val width = (metrics.widthPixels * densityAdjustment).toInt()
                val adSize= AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, width)
                banner.setAdSize(adSize)
            }



            banner.adUnitId=id

            banner.adListener=object : AdListener(){
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    if(mrec){
                        frameLayout.setBackgroundColor(Color.TRANSPARENT)
                    }
                    frameLayout.removeAllViews()
                    frameLayout.visibility= View.VISIBLE
                    frameLayout.addView(banner)
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)


                    Log.e("ADS", "onAdFailedToLoad: "+p0.message )
                }
            }


            val request:AdRequest = if(collapsable){
                val extras = Bundle()
                extras.putString("collapsible", "bottom")
                AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                    .build()

            }else{
                AdRequest.Builder().build()
            }


            banner.loadAd(request)
            isLoading=true


        }


        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            if(::adView.isInitialized){
                adView.pause()
            }
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            if(::adView.isInitialized){
                adView.resume()
            }
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            if(::adView.isInitialized){
                adView.destroy()
            }


        }
    }




    companion object{
        private fun inflateNativeView(
            nativeAd: NativeAd,
            binding: NativeViewBinding, showMedia:Boolean
        ): NativeAdView {
            val nativeAdView = binding.root


            nativeAdView.iconView = binding.adAppIcon
            nativeAdView.headlineView = binding.adAdvertiser
            nativeAdView.bodyView=binding.tvBody
            nativeAdView.starRatingView=binding.ratingBar


            nativeAdView.callToActionView = binding.adCallToAction

            binding.adAdvertiser.text = nativeAd.headline
            nativeAd.mediaContent?.let {
                binding.mediaview.mediaContent = it
            }

            if (nativeAd.callToAction == null) {
                binding.adCallToAction.visibility = View.INVISIBLE
            } else {
                binding.adCallToAction.visibility = View.VISIBLE
                binding.adCallToAction.text = nativeAd.callToAction
            }

            if (nativeAd.body == null) {
                binding.tvBody.visibility = View.INVISIBLE

                binding.tvBody.isSelected=true
            } else {

                binding.tvBody.text = nativeAd.body
            }
            if (nativeAd.icon == null) {
                binding.adAppIcon.visibility = View.INVISIBLE
            } else {
                binding.adAppIcon.visibility = View.VISIBLE
                binding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
            }
            if (nativeAd.starRating != null) {

                binding.ratingBar.rating=nativeAd.starRating!!.toFloat()

            }
            if(showMedia) {
                binding.mediaview.visibility=View.VISIBLE
                nativeAdView.mediaView = binding.mediaview

            }else {
                binding.mediaview.visibility=View.GONE
            }
            nativeAdView.setNativeAd(nativeAd)

            if(showMedia) {
                val mediaContent = nativeAd.mediaContent
                val vc = mediaContent?.videoController

                if (vc != null && mediaContent.hasVideoContent()) {
                    vc.videoLifecycleCallbacks =
                        object : VideoController.VideoLifecycleCallbacks() {
                        }
                }
            }
            binding.tvBody.isSelected=true
            return nativeAdView


        }
    }






   private class AdmobNative(c:AppCompatActivity):DefaultLifecycleObserver {
        val context=c
        var isLoading=false
        var nativeAd: NativeAd?=null

        init {
            context.lifecycle.addObserver(this)
        }



        fun loadAndShowNative(frameLayout: FrameLayout,id2:String,showMedia: Boolean){
            if(isLoading)return

            val id=if(BuildConfig.DEBUG){
                "ca-app-pub-3940256099942544/2247696110"
            }else{
              id2
            }

            val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()
            val nativeAdOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
            val adRequest = AdRequest.Builder().build()


            val listener=object :AdListener(){
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)

                    isLoading=false
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    isLoading=false

                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    nativeAd?.apply {

                    }
                    if(nativeAd!=null){
                        frameLayout.visibility=View.VISIBLE
                        frameLayout.removeAllViews()
                        frameLayout.addView(
                            inflateNativeView(nativeAd!!,
                            NativeViewBinding.inflate(LayoutInflater.from(context)),showMedia)
                        )

                    }else{
                        frameLayout.visibility=View.GONE
                    }


                }
            }
            val adLoader= AdLoader.Builder(context,id).forNativeAd {

                if(nativeAd!=null){
                    nativeAd?.destroy()
                }
                nativeAd=it

            }.withNativeAdOptions(nativeAdOptions)
                .withAdListener(listener)

                .build()


            adLoader.loadAd(adRequest)
            isLoading=true




        }

        override fun onDestroy(owner: LifecycleOwner) {
            isLoading=false
            nativeAd?.destroy()
            super.onDestroy(owner)

        }



    }

    private var interstitialAd: InterstitialAd?=null
    private var isloading=false
    fun preloadInter(id2: String,listener: AwesomeListener?=null){
        if(isloading|| interstitialAd!=null)return

        val id=if(BuildConfig.DEBUG){
            "ca-app-pub-3940256099942544/1033173712"
        }else{
          id2
        }

        val request=AdRequest.Builder().setHttpTimeoutMillis(15*1000).build()

        InterstitialAd.load(applicationContext,id,request,object :
            InterstitialAdLoadCallback(){
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                isloading=false

                listener?.onComplete(false)
            }

            override fun onAdLoaded(p0: InterstitialAd) {
                super.onAdLoaded(p0)
                interstitialAd=p0
                listener?.onComplete(true)
            }
        })

    }



    fun isInterAvailable():Boolean{
        return interstitialAd!=null
    }
    fun showInterIfAvail( id:String,onDismiss:Runnable, showLoading:Boolean=true){

        val params= WindowManager.LayoutParams()
        val view= AdLoadingBinding.inflate(LayoutInflater.from(act))


        if(showLoading){
            act.windowManager.addView(view.root,params)
        }

        if( interstitialAd!=null){

            if(showLoading){
                view.root.postDelayed({

                    act.windowManager.removeView(view.root)
                    showInter(act,id,onDismiss)
                },1500)
            }else{
                showInter(act,id,onDismiss)
            }


        }else{

            if(showLoading){
                act.windowManager.removeView(view.root)
            }
            onDismiss.run()

        }
    }

    fun showRunTimeInter(id2: String, onDismiss:Runnable, showLoading:Boolean=true){
        val params= WindowManager.LayoutParams()
        val view= AdLoadingBinding.inflate(LayoutInflater.from(act))


        if(showLoading){
            act.windowManager.addView(view.root,params)
        }

        if( interstitialAd!=null){

            if(showLoading){
                view.root.postDelayed({
                    act.windowManager.removeView(view.root)
                    showInter(act,id2,onDismiss)
                },1500)
            }else{
                showInter(act,id2,onDismiss)
            }

        }else{


            val id=if(BuildConfig.DEBUG){
                "ca-app-pub-3940256099942544/1033173712"
            }else{
                id2
            }

            preloadInter(id,object:AwesomeListener{
                override fun onComplete(isLoaded: Boolean) {
                    if(showLoading){
                        act.windowManager.removeView(view.root)
                    }

                    showInter(act,id,onDismiss)

                }
            })


        }
    }

    private fun showInter(act: Activity,id:String,onDismiss: Runnable,reload:Boolean=true){

        if(interstitialAd!=null){
            interstitialAd?.let {

                it.fullScreenContentCallback=object :FullScreenContentCallback(){
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()

                        interstitialAd=null
                        isloading=false


                        if(reload){
                            preloadInter(id)
                        }
                        onDismiss.run()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        interstitialAd=null
                        isloading=false
                        onDismiss.run()

                    }
                }

                it.show(act)

            }
        }else{

            onDismiss.run()
        }

    }


    interface AwesomeListener{
       fun onComplete(isLoaded:Boolean)

    }



}