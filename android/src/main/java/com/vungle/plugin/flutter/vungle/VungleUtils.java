package com.vungle.plugin.flutter.vungle;

import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

public class VungleUtils {
    public static Vungle.Consent strToConsentStatus(String consent) {
        switch (consent) {
            case "Accepted":
                return Vungle.Consent.OPTED_IN;
            case "Denied":
                return Vungle.Consent.OPTED_OUT;
            default:
                return null;
        }
    }

    public static String consentStatusToStr(Vungle.Consent consent) {
        switch (consent) {
            case OPTED_IN:
                return "Accepted";
            case OPTED_OUT:
                return "Denied";
            default:
                return "";
        }
    }

    public static String convertError(VungleException exception) {
        switch (exception.getExceptionCode()) {
            case VungleException.NO_SERVE:
                return "noServe";
            case VungleException.UNKNOWN_ERROR:
                return "unknownError";
            case VungleException.CONFIGURATION_ERROR:
                return "configurationError";
            case VungleException.AD_EXPIRED:
                return "adExpired";
            case VungleException.UNSUPPORTED_CONFIGURATION:
                return "unsupportedConfiguration";
            case VungleException.MISSING_REQUIRED_ARGUMENTS_FOR_INIT:
                return "missingRequiredArgumentsForInit";
            case VungleException.APPLICATION_CONTEXT_REQUIRED:
                return "applicationContextRequired";
            case VungleException.OPERATION_ONGOING:
                return "operationOngoing";
            case VungleException.VUNGLE_NOT_INTIALIZED:
                return "vungleNotInitialized";
            case VungleException.AD_UNABLE_TO_PLAY:
                return "adUnableToPlay";
            case VungleException.AD_FAILED_TO_DOWNLOAD:
                return "adFailedToDownload";
            case VungleException.NO_AUTO_CACHED_PLACEMENT:
                return "noAutoCachedPlacement";
            case VungleException.PLACEMENT_NOT_FOUND:
                return "placementNotFound";
            case VungleException.SERVER_RETRY_ERROR:
                return "serverRetryError";
            case VungleException.ALREADY_PLAYING_ANOTHER_AD:
                return "alreadyPlayingAnotherAd";
            case VungleException.NO_SPACE_TO_INIT:
                return "noSpaceToInit";
            case VungleException.NO_SPACE_TO_LOAD_AD:
                return "noSpaceToLoadAd";
            case VungleException.NO_SPACE_TO_LOAD_AD_AUTO_CACHED:
                return "noSpaceToLoadAdAutoCached";
            case VungleException.NO_SPACE_TO_DOWNLOAD_ASSETS:
                return "noSpaceToDownloadAssets";
            case VungleException.NETWORK_ERROR:
                return "networkError";
            case VungleException.SERVER_ERROR:
                return "serverError";
            case VungleException.SERVER_TEMPORARY_UNAVAILABLE:
                return "serverTemporaryUnavailable";
            case VungleException.ASSET_DOWNLOAD_RECOVERABLE:
                return "assetDownloadRecoverable";
            case VungleException.ASSET_DOWNLOAD_ERROR:
                return "assetDownloadError";
            case VungleException.OPERATION_CANCELED:
                return "operationCanceled";
            case VungleException.DB_ERROR:
                return "dbError";
            case VungleException.RENDER_ERROR:
                return "renderError";
            case VungleException.INVALID_SIZE:
                return "invalidSize";
            case VungleException.INCORRECT_DEFAULT_API_USAGE:
                return "incorrectDefaultApiUsage";
            case VungleException.INCORRECT_BANNER_API_USAGE:
                return "incorrectBannerApiUsage";
            case VungleException.WEB_CRASH:
                return "webCrash";
            case VungleException.WEBVIEW_RENDER_UNRESPONSIVE:
                return "webviewRenderUnresponsive";
            case VungleException.NETWORK_UNREACHABLE:
                return "networkUnreachable";
            case VungleException.NETWORK_PERMISSIONS_NOT_GRANTED:
                return "networkPermissionsNotGranted";
            case VungleException.SDK_VERSION_BELOW_REQUIRED_VERSION:
                return "sdkVersionBelowRequiredVersion";
            case VungleException.MISSING_HBP_EVENT_ID:
                return "missingHbpEventId";
            case VungleException.AD_PAST_EXPIRATION:
                return "adPastExpiration";
            case VungleException.AD_RENDER_NETWORK_ERROR:
                return "adRenderNetworkError";
            default:
                return "";
        }
    }
}
