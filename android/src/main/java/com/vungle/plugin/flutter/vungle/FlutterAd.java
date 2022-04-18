package com.vungle.plugin.flutter.vungle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vungle.warren.error.VungleException;

import io.flutter.plugin.platform.PlatformView;

abstract class FlutterAd {

    protected final int adId;

    FlutterAd(int adId) {
        this.adId = adId;
    }

    /** Wrapper for {@link VungleException}. */
    static class FlutterVungleException {
        final int code;
        final String message;

        FlutterVungleException(@NonNull VungleException exception) {
            code = exception.getExceptionCode();
            message = exception.getLocalizedMessage();
        }

        FlutterVungleException(int code) {
            this.code = code;
            this.message = new VungleException(code).getLocalizedMessage();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (!(object instanceof FlutterVungleException)) {
                return false;
            }

            final FlutterVungleException that = (FlutterVungleException) object;

            return code == that.code;
        }

        @Override
        public int hashCode() {
            return code;
        }
    }

    abstract void load();

    /**
     * Gets the PlatformView for the ad. Default behavior is to return null. Should be overridden by
     * ads with platform views, such as banner ads.
     */
    @Nullable
    PlatformView getPlatformView() {
        return null;
    }

    /**
     * Invoked when dispose() is called on the corresponding Flutter ad object. This perform any
     * necessary cleanup.
     */
    abstract void dispose();
}
