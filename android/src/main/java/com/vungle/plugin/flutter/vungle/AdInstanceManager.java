package com.vungle.plugin.flutter.vungle;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

/**
 * Maintains reference to ad instances for the {@link
 * com.vungle.plugin.flutter.vungle.VunglePlugin}.
 *
 * <p>When an Ad is loaded from Dart, an equivalent ad object is created and maintained here to
 * provide access until the ad is disposed.
 */
class AdInstanceManager {
    @Nullable private Activity activity;

    @NonNull private final Map<Integer, FlutterAd> ads;
    @NonNull private final MethodChannel channel;

    /**
     * Initializes the ad instance manager. We only need a method channel to start loading ads, but an
     * activity must be present in order to attach any ads to the view hierarchy.
     */
    AdInstanceManager(@NonNull MethodChannel channel) {
        this.channel = channel;
        this.ads = new HashMap<>();
    }

    void setActivity(@Nullable Activity activity) {
        this.activity = activity;
    }

    @Nullable
    Activity getActivity() {
        return activity;
    }

    @Nullable
    FlutterAd adForId(int id) {
        return ads.get(id);
    }

    @Nullable
    Integer adIdFor(@NonNull FlutterAd ad) {
        for (Integer adId : ads.keySet()) {
            if (ads.get(adId) == ad) {
                return adId;
            }
        }
        return null;
    }

    void trackAd(@NonNull FlutterAd ad, int adId) {
        if (ads.get(adId) != null) {
            throw new IllegalArgumentException(
                    String.format("Ad for following adId already exists: %d", adId));
        }
        ads.put(adId, ad);
    }

    void disposeAd(int adId) {
        if (!ads.containsKey(adId)) {
            return;
        }
        FlutterAd ad = ads.get(adId);
        if (ad != null) {
            ad.dispose();
        }
        ads.remove(adId);
    }

    void disposeAllAds() {
        for (Map.Entry<Integer, FlutterAd> entry : ads.entrySet()) {
            if (entry.getValue() != null) {
                entry.getValue().dispose();
            }
        }
        ads.clear();
    }

    void onAdLoad(int adId) {
        Map<Object, Object> arguments = new HashMap<>();
        arguments.put("adId", adId);
        arguments.put("eventName", "onAdLoad");
        invokeOnAdEvent(arguments);
    }

    void onAdLoadError(int adId, @NonNull FlutterAd.FlutterVungleException exception) {
        Map<Object, Object> arguments = new HashMap<>();
        arguments.put("adId", adId);
        arguments.put("eventName", "onAdLoadError");
        arguments.put("error", exception);
        invokeOnAdEvent(arguments);
    }

    void onAdStart(int adId) {
        Map<Object, Object> arguments = new HashMap<>();
        arguments.put("adId", adId);
        arguments.put("eventName", "onAdStart");
        invokeOnAdEvent(arguments);
    }

    void onAdViewed(int adId) {
        Map<Object, Object> arguments = new HashMap<>();
        arguments.put("adId", adId);
        arguments.put("eventName", "onAdViewed");
        invokeOnAdEvent(arguments);
    }

    void onAdEnd(int adId) {
        Map<Object, Object> arguments = new HashMap<>();
        arguments.put("adId", adId);
        arguments.put("eventName", "onAdEnd");
        invokeOnAdEvent(arguments);
    }

    void onAdClick(int adId) {
        Map<Object, Object> arguments = new HashMap<>();
        arguments.put("adId", adId);
        arguments.put("eventName", "onAdClick");
        invokeOnAdEvent(arguments);
    }

    void onAdLeftApplication(int adId) {
        Map<Object, Object> arguments = new HashMap<>();
        arguments.put("adId", adId);
        arguments.put("eventName", "onAdLeftApplication");
        invokeOnAdEvent(arguments);
    }

    void onAdPlayError(int adId, @NonNull FlutterAd.FlutterVungleException exception) {
        Map<Object, Object> arguments = new HashMap<>();
        arguments.put("adId", adId);
        arguments.put("eventName", "onAdPlayError");
        arguments.put("error", exception);
        invokeOnAdEvent(arguments);
    }

    /** Invoke the method channel using the UI thread. Otherwise the message gets silently dropped. */
    private void invokeOnAdEvent(final Map<Object, Object> arguments) {
        new Handler(Looper.getMainLooper())
                .post(
                        new Runnable() {
                            @Override
                            public void run() {
                                channel.invokeMethod("onAdEvent", arguments);
                            }
                        });
    }
}
