package com.aegis.pdf.core.premium

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val premiumManager: PremiumManager
) {

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private val adUnitIds = mapOf(
        "interstitial" to "ca-app-pub-3940256099942544/1033173712", // Test ID
        "rewarded" to "ca-app-pub-3940256099942544/5224354917"       // Test ID
    )

    init {
        MobileAds.initialize(context)
        loadAds()
    }

    private fun loadAds() {
        if (premiumManager.isPremium) return

        loadInterstitial()
        loadRewarded()
    }

    private fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, adUnitIds["interstitial"]!!, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            })
    }

    private fun loadRewarded() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, adUnitIds["rewarded"]!!, adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            })
    }

    fun showInterstitial(activity: Activity, onDismissed: () -> Unit = {}) {
        if (premiumManager.isPremium || interstitialAd == null) {
            onDismissed()
            return
        }
        interstitialAd?.show(activity)
        interstitialAd = null
        loadInterstitial()
        onDismissed()
    }

    fun showRewarded(
        activity: Activity,
        onReward: (Int, String) -> Unit,
        onDismissed: () -> Unit = {}
    ) {
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onDismissed()
                    loadRewarded()
                }
            }
            ad.show(activity) { reward ->
                onReward(reward.amount, reward.type)
            }
        } ?: onDismissed()
    }
}