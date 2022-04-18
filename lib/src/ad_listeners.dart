import 'package:flutter/foundation.dart';

import 'ad_containers.dart';

/// The callback type to handle an event occurring for an [Ad].
typedef AdEventCallback = void Function(Ad ad);

/// The callback type to handle an error loading an [Ad].
typedef AdErrorCallback = void Function(Ad ad, VungleException exception);

/// Shared event callbacks used in Banner ads.
abstract class AdWithViewListener {
  /// Default constructor for [AdWithViewListener], meant to be used by subclasses.
  @protected
  const AdWithViewListener({
    this.onAdLoad,
    this.onAdLoadError,
    this.onAdStart,
    this.onAdViewed,
    this.onAdEnd,
    this.onAdClick,
    this.onAdLeftApplication,
    this.onAdPlayError,
  });

  /// Called when the ad has been successfully loaded and be played for the
  /// placement
  final AdEventCallback? onAdLoad;

  /// Called when an error occurs while attempting to play an ad.
  final AdErrorCallback? onAdLoadError;

  /// Called when the Vungle SDK has successfully launched the advertisement
  /// and an advertisement will begin playing momentarily.
  final AdEventCallback? onAdStart;

  /// Called when the ad is first rendered on device.
  /// Please use this callback to track impressions.
  final AdEventCallback? onAdViewed;

  /// Called when the entire ad experience has been completed, just before
  /// the control has been returned back to the hosting app.
  final AdEventCallback? onAdEnd;

  /// Called when the user has clicked on a video ad or download button.
  final AdEventCallback? onAdClick;

  /// Called when the user leaves the app before ad experience is completed,
  /// such as opening the Store page for the ad.
  final AdEventCallback? onAdLeftApplication;

  /// Called when an error occurs while attempting to play an ad.
  final AdErrorCallback? onAdPlayError;
}

/// A listener for receiving notifications for the lifecycle of a [BannerAd].
class BannerAdListener extends AdWithViewListener {
  /// Constructs a [BannerAdListener] that notifies for the provided event callbacks.
  ///
  /// Typically you will override [onAdLoad] and [onAdLoadError]:
  /// ```dart
  /// BannerAdListener(
  ///   onAdLoad: (ad) {
  ///     // Ad successfully loaded - display an AdWidget with the banner ad.
  ///   },
  ///   onAdLoadError: (ad, exception) {
  ///     // Ad failed to load - log the error and dispose the ad.
  ///   },
  ///   ...
  /// )
  /// ```
  const BannerAdListener({
    AdEventCallback? onAdLoad,
    AdErrorCallback? onAdLoadError,
    AdEventCallback? onAdStart,
    AdEventCallback? onAdViewed,
    AdEventCallback? onAdEnd,
    AdEventCallback? onAdClick,
    AdEventCallback? onAdLeftApplication,
    AdErrorCallback? onAdPlayError,
  }) : super(
    onAdLoad: onAdLoad,
    onAdLoadError: onAdLoadError,
    onAdStart: onAdStart,
    onAdViewed: onAdViewed,
    onAdEnd: onAdEnd,
    onAdClick: onAdClick,
    onAdLeftApplication: onAdLeftApplication,
    onAdPlayError: onAdPlayError,
  );
}
