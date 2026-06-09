package com.example

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError

object AdMobManager {
    private var rewardedAd: RewardedAd? = null

    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null) return

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            "ca-app-pub-4288324218526190/8832383188",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("AdMob", "Ad failed to load: ${adError.message}")
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("AdMob", "Ad was loaded.")
                    rewardedAd = ad
                }
            })
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit) {
        if (rewardedAd != null) {
            var rewardEarned = false
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("AdMob", "Ad was dismissed.")
                    rewardedAd = null
                    loadRewardedAd(activity) // Preload next ad
                    if (rewardEarned) {
                        onRewardEarned()
                    } else {
                        onAdDismissed()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d("AdMob", "Ad failed to show: ${adError.message}")
                    rewardedAd = null
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("AdMob", "Ad showed fullscreen content.")
                }
            }

            rewardedAd?.show(activity) { rewardItem ->
                Log.d("AdMob", "User earned the reward.")
                rewardEarned = true
            }
        } else {
            Log.d("AdMob", "The rewarded ad wasn't ready yet.")
            onAdDismissed() // Fallback if ad isn't loaded
        }
    }
}
