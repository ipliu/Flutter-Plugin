package com.vungle.plugin.flutter.vungle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vungle.warren.BannerAdConfig;
import com.vungle.warren.Banners;
import com.vungle.warren.VungleBanner;

import io.flutter.plugin.platform.PlatformView;
import io.flutter.util.Preconditions;

/** A wrapper for {@link VungleBanner}. */
class FlutterBannerAd extends FlutterAd implements FlutterAdLoadCallback {

    @NonNull private final AdInstanceManager manager;
    @NonNull private final String placementId;
    @NonNull private final FlutterAdSize size;
    @Nullable private VungleBanner vungleBanner;

    FlutterBannerAd(
            int adId,
            @NonNull AdInstanceManager manager,
            @NonNull String placementId,
            @NonNull FlutterAdSize size) {
        super(adId);
        Preconditions.checkNotNull(manager);
        Preconditions.checkNotNull(placementId);
        Preconditions.checkNotNull(size);
        this.manager = manager;
        this.placementId = placementId;
        this.size = size;
    }

    @Override
    public void onAdLoad() {
        manager.onAdLoad(adId);
        final BannerAdConfig bannerAdConfig = new BannerAdConfig(size.getAdSize());
        if (Banners.canPlayAd(placementId, bannerAdConfig.getAdSize())) {
            vungleBanner = Banners.getBanner(
                    placementId, bannerAdConfig, new FlutterPlayAdCallback(adId, manager));
        }
    }

    @Override
    void load() {
        final BannerAdConfig bannerAdConfig = new BannerAdConfig();
        bannerAdConfig.setAdSize(size.getAdSize());
        Banners.loadBanner(
                placementId,
                bannerAdConfig,
                new FlutterBannerLoadAdCallback(adId, manager, this));
    }

    @Nullable
    @Override
    public PlatformView getPlatformView() {
        if (vungleBanner == null) {
            return null;
        }

        vungleBanner.disableLifeCycleManagement(true);
        vungleBanner.renderAd();
        vungleBanner.setAdVisibility(true);

        return new FlutterPlatformView(vungleBanner);
    }

    @Override
    void dispose() {
        if (vungleBanner != null) {
            vungleBanner.setAdVisibility(false);
            vungleBanner.destroyAd();
            vungleBanner = null;
        }
    }

    @Nullable
    FlutterAdSize getAdSize() {
        return size;
    }
}
