/// Error category of load errors
enum VungleError {
  /// No advertisements are available
  noServe,

  /// Unknown error
  unknownError,

  /// Configuration error
  configurationError,

  /// Advertisement in the cache has expired
  adExpired,

  /// Placement not configured properly
  unsupportedConfiguration,

  /// Missing required arguments for init
  missingRequiredArgumentsForInit,

  /// Application context required
  applicationContextRequired,

  /// Already an ongoing operation for the action
  operationOngoing,

  /// Not initialized
  vungleNotInitialized,

  /// Unable to play advertisement
  adUnableToPlay,

  /// Advertisement failed to download
  adFailedToDownload,

  /// No auto-cached Placement on config
  noAutoCachedPlacement,

  /// Placement is not valid
  placementNotFound,

  /// Remote Server responded with http Retry-After
  serverRetryError,

  /// Vungle is already playing different ad
  alreadyPlayingAnotherAd,

  /// Not enough file system size on a device to initialize
  noSpaceToInit,

  /// Not enough file system size on a device to request an ad
  noSpaceToLoadAd,

  /// Not enough file system size on a device to request an ad for auto cache
  noSpaceToLoadAdAutoCached,

  /// Not enough file system size on a device to download assets for an ad
  noSpaceToDownloadAssets,

  /// Network error
  networkError,

  /// Server error
  serverError,

  /// Server temporary unavailable
  serverTemporaryUnavailable,

  /// Assets download failed
  assetDownloadRecoverable,

  /// Assets download failed
  assetDownloadError,

  /// Operation was canceled
  operationCanceled,

  /// Database error
  dbError,

  /// Render error
  renderError,

  /// Ad size is invalid
  invalidSize,

  /// Cannot request or play MREC or FullScreen Ads from Banner API
  incorrectDefaultApiUsage,

  /// Cannot request or play Banner Ads from Vungle API
  incorrectBannerApiUsage,

  /// Android web view has crashed
  webCrash,

  /// Android web view render became unresponsive
  webviewRenderUnresponsive,

  /// Network unreachable
  networkUnreachable,

  /// Network permissions not granted
  networkPermissionsNotGranted,

  /// SDK minimum version is below required version
  sdkVersionBelowRequiredVersion,

  /// No Event id passed for HBP
  missingHbpEventId,

  /// Cached Ad is no longer available due to expired timestamp
  adPastExpiration,

  /// Ad rendering failed due to network connectivity issue
  adRenderNetworkError,
}

VungleError errorFromString(String error) {
  return VungleError.values.firstWhere(
          (e) => error == e.toString().split('.').last,
      orElse: () => VungleError.unknownError);
}