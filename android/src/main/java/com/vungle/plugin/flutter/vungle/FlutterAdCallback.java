package com.vungle.plugin.flutter.vungle;

import androidx.annotation.NonNull;

import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.error.VungleException;

import java.lang.ref.WeakReference;

/** Callback type to notify when an ad successfully loads. */
interface FlutterAdLoadCallback {
    void onAdLoad();
}

class FlutterAdCallback {
    protected final int adId;
    @NonNull protected final AdInstanceManager manager;

    FlutterAdCallback(int adId, @NonNull AdInstanceManager manager) {
        this.adId = adId;
        this.manager = manager;
    }
}

class FlutterBannerLoadAdCallback extends FlutterAdCallback implements LoadAdCallback {

    @NonNull final WeakReference<FlutterAdLoadCallback> adLoadCallbackWeakReference;

    FlutterBannerLoadAdCallback(
            int adId, @NonNull AdInstanceManager manager, FlutterAdLoadCallback adLoadCallback) {
        super(adId, manager);
        adLoadCallbackWeakReference = new WeakReference<>(adLoadCallback);
    }

    @Override
    public void onAdLoad(String placementId) {
        if (adLoadCallbackWeakReference.get() != null) {
            adLoadCallbackWeakReference.get().onAdLoad();
        }
    }

    @Override
    public void onError(String placementId, VungleException exception) {
        manager.onAdLoadError(adId, new FlutterAd.FlutterVungleException(exception));
    }
}

class FlutterPlayAdCallback extends FlutterAdCallback implements PlayAdCallback {

    FlutterPlayAdCallback(int adId, @NonNull AdInstanceManager manager) {
        super(adId, manager);
    }

    @Override
    public void creativeId(String creativeId) { }

    @Override
    public void onAdStart(String placementId) {
        manager.onAdStart(adId);
    }

    @Deprecated
    @Override
    public void onAdEnd(String placementId, boolean completed, boolean isCTAClicked) { }

    @Override
    public void onAdEnd(String placementId) {
        manager.onAdEnd(adId);
    }

    @Override
    public void onAdClick(String placementId) {
        manager.onAdClick(adId);
    }

    @Override
    public void onAdRewarded(String placementId) { }

    @Override
    public void onAdLeftApplication(String placementId) {
        manager.onAdLeftApplication(adId);
    }

    @Override
    public void onError(String placementId, VungleException exception) {
        manager.onAdPlayError(adId, new FlutterAd.FlutterVungleException(exception));
    }

    @Override
    public void onAdViewed(String placementId) {
        manager.onAdViewed(adId);
    }
}
