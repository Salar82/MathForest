package ir.alishojaee.mathforest.utils

import android.app.Activity
import android.view.ViewGroup
import ir.alishojaee.mathforest.BuildConfig
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusBannerType
import ir.tapsell.plus.TapsellPlusInitListener
import ir.tapsell.plus.model.AdNetworkError
import ir.tapsell.plus.model.AdNetworks
import ir.tapsell.plus.model.TapsellPlusAdModel
import ir.tapsell.plus.model.TapsellPlusErrorModel

interface AdShowStatusListener {
    fun onSuccess()
    fun onFailed()
}

class TapsellAdManager(var activity: Activity) {
    private var standardResponseId: String = ""
    private var interstitialResponseId: String = ""
    private var rewardedVideoResponseId: String = ""
    fun initAd() {
        TapsellPlus.initialize(
            activity, BuildConfig.TAPSELL_KEY,
            object : TapsellPlusInitListener {
                override fun onInitializeSuccess(adNetworks: AdNetworks) {
                    requestStandardBanner()
                    requestInterstitial()
                    requestRewardedVideo()
                }

                override fun onInitializeFailed(
                    adNetworks: AdNetworks,
                    adNetworkError: AdNetworkError
                ) {

                }
            }
        )
    }

    fun retryRequest() {
        requestStandardBanner()
        requestInterstitial()
        requestRewardedVideo()
    }

    fun showExitStandardBanner(view: ViewGroup) {
        if (standardResponseId.isNotEmpty()) {
            TapsellPlus.showStandardBannerAd(
                activity, standardResponseId, view,
                object : AdShowListener() {
                    override fun onOpened(p0: TapsellPlusAdModel?) {
                        requestStandardBanner()
                    }
                    override fun onError(p0: TapsellPlusErrorModel?) {}
                }
            )
        }
    }

    fun showRewardedVideo(listener: AdShowStatusListener) {
        TapsellPlus.showRewardedVideoAd(
            activity, rewardedVideoResponseId,
            object : AdShowListener() {
                override fun onOpened(tapsellPlusAdModel: TapsellPlusAdModel?) {
                    super.onOpened(tapsellPlusAdModel)
                    requestRewardedVideo()
                }

                override fun onRewarded(p0: TapsellPlusAdModel?) {
                    super.onRewarded(p0)
                    listener.onSuccess()
                }
                override fun onError(tapsellPlusErrorModel: TapsellPlusErrorModel?) {
                    super.onError(tapsellPlusErrorModel)
                    listener.onFailed()
                }
            })
    }

    fun showInterstitialBanner(listener: AdShowStatusListener) {
        if (interstitialResponseId.isNotEmpty()) {
            TapsellPlus.showInterstitialAd(
                activity, interstitialResponseId,
                object : AdShowListener() {
                    override fun onOpened(tapsellPlusAdModel: TapsellPlusAdModel?) {
                        super.onOpened(tapsellPlusAdModel)
                        listener.onSuccess()
                        requestInterstitial()
                    }

                    override fun onError(tapsellPlusErrorModel: TapsellPlusErrorModel?) {
                        super.onError(tapsellPlusErrorModel)
                        listener.onFailed()
                    }
                })
        } else {
            listener.onFailed()
        }
    }

    private fun requestStandardBanner() {
        TapsellPlus.requestStandardBannerAd(
            activity, BuildConfig.TAPSELL_STANDARD_ZONE_ID,
            TapsellPlusBannerType.BANNER_320x50,
            object : AdRequestCallback() {
                override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                    super.response(tapsellPlusAdModel)
                    standardResponseId = tapsellPlusAdModel.responseId
                }

                override fun error(message: String?) {}
            }
        )
    }

    private fun requestInterstitial() {
        TapsellPlus.requestInterstitialAd(
            activity,
            BuildConfig.TAPSELL_INSTERTITIAL_ZONE_ID,
            object : AdRequestCallback() {
                override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                    super.response(tapsellPlusAdModel)
                    interstitialResponseId = tapsellPlusAdModel.responseId
                }

                override fun error(message: String?) {}
            }
        )
    }

    private fun requestRewardedVideo() {
        TapsellPlus.requestRewardedVideoAd(
            activity,
            BuildConfig.TAPSELL_REWARDED_VIDEO_ZONE_ID,
            object : AdRequestCallback() {
                override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                    super.response(tapsellPlusAdModel)
                    rewardedVideoResponseId = tapsellPlusAdModel.responseId
                }

                override fun error(message: String?) {}
            }
        )
    }
}