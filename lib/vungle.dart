import 'dart:async';

import 'package:flutter/services.dart';

import 'constants.dart';
import 'vungle_error.dart';
import 'src/ad_instance_manager.dart';

export 'src/ad_containers.dart';
export 'src/ad_listeners.dart';

/// User Consent status
///
/// This is for GDPR Users
enum UserConsentStatus {
  Accepted,
  Denied,
}

class Vungle {
  static final MethodChannel _channel = MethodChannel(
      MAIN_CHANNEL, StandardMethodCodec(AdMessageCodec()));

  static final Map<String, _AdMethodChannel> _adChannels = {};

  /// Get version of Vungle native SDK
  static Future<String> getSDKVersion() async {
    return await _channel.invokeMethod('sdkVersion', <String, dynamic>{}) ?? '';
  }

  /// Initialize the flutter plugin for Vungle SDK.
  ///
  /// Please go to the http://www.vungle.com to apply for a publisher account and register your apps.
  /// Then you will get an [appId] for each of your apps. You need to provide the id when calling this method.
  /// Note: If you want to use flutter to develop an app for both iOS and Android, you need register two apps for each platform on vungle dashboard.
  /// And use following code to initialize the plugin:
  /// ```dart
  /// if(Platform.isAndroid) {
  ///   Vungle.init('<Your-Android-AppId>');
  /// } else if(Platform.isIOS) {
  ///   Vungle.init('<Your-iOS-AppId>');
  /// }
  ///
  /// Vungle.onInitilizeListener = () {
  ///   //SDK has initialized, could load ads now
  /// }
  /// ```
  static Future<void> init({
    required String appId,
    Function? onComplete,
    Function(VungleError error, String errorMessage)? onFailed,
    Function(String placementId)? onAutoCacheAd,
  }) async {
    final arguments = <String, dynamic>{
      APP_ID_PARAMETER: appId,
    };
    /// Internal init to cleanup state for hot restart.
    /// This is a workaround for https://github.com/flutter/flutter/issues/7160.
    _channel.invokeMethod('_init');

    //register callback method handler
    _channel.setMethodCallHandler((call) =>
        _initMethodCall(call, onComplete, onFailed, onAutoCacheAd));
    await _channel.invokeMethod(INIT_METHOD, arguments);
  }

  static Future<dynamic> _initMethodCall(
      MethodCall call,
      Function? onComplete,
      Function(VungleError, String)? onFailed,
      Function(String)? onAutoCacheAd,
      ) {
    switch (call.method) {
      case INIT_COMPLETE_METHOD:
        onComplete?.call();
        break;
      case INIT_FAILED_METHOD:
        onFailed?.call(
            errorFromString(call.arguments[ERROR_CODE_PARAMETER]),
            call.arguments[ERROR_MESSAGE_PARAMETER]);
        break;
      case INIT_AUTO_CACHE_AD_METHOD:
        onAutoCacheAd?.call(call.arguments[PLACEMENT_ID_PARAMETER]);
        break;
    }
    return Future.value(true);
  }

  /// Enable background download for Vungle iOS SDK.
  ///
  /// Please note that this API is called before calling init(),
  /// and only takes effect on iOS side.
  ///
  /// ```
  static void enableBackgroundDownload(bool enabled) {
    _channel.invokeMethod('enableBackgroundDownload', <String, dynamic>{
      'enabled': enabled,
    });
  }

  /// Load Ad by a [placementId]
  ///
  /// After you registered your apps on the vungle dashboard, you will get a default placement with an id for each app.
  /// You could create more placements as you want. And You need call this method to load the ads for the placement.
  /// You could use [onAdPlayableListener] to know when the ad loaded.
  /// ```dart
  /// if(Platform.isAndroid) {
  ///   Vungle.loadAd('<Your-Android-placementId>')
  /// } else if(Platform.isIOS) {
  ///   Vungle.loadAd('<Your-IOS-placementId>')
  /// }
  ///
  /// Vungle.onAdPlayableListener = (playable, placementId) {
  ///   if(playable) {
  ///     //the ad is loaded, could play ad for now.
  ///   }
  /// }
  /// ```
  static Future<void> loadAd({
    required String placementId,
    Function(String placementId)? onComplete,
    Function(String placementId, VungleError error, String errorMessage)?
    onFailed,
  }) async {
    _adChannels
        .putIfAbsent(placementId, () => _AdMethodChannel(placementId))
        .update(
      onLoadComplete: onComplete,
      onLoadFailed: onFailed,
    );

    final arguments = <String, dynamic>{
      PLACEMENT_ID_PARAMETER: placementId,
    };
    await _channel.invokeMethod(LOAD_METHOD, arguments);
  }

  /// Play ad by a [placementId]
  ///
  /// When ad is loaded, you could call this method to play ad
  /// ```dart
  /// Vungle.onAdPlayableListener = (playable, placementId) {
  ///   if(playable) {
  ///     Vungle.playAd(placementId);
  ///   }
  /// }
  ///
  /// Vungle.onAdStartedListener = (placementId) {
  ///   //ad started to play
  /// }
  ///
  /// Vungle.onAdFinishedListener = (placementId, isCTAClicked, isCompletedView) {
  ///   if(isCTAClicked) {
  ///     //User has clicked the download button
  ///   }
  ///   if(isCompletedView) {
  ///     //User has viewed the ad completely
  ///   }
  /// }
  /// ```
  static Future<void> playAd({
    required String placementId,
    Function(String placementId)? onStart,
    Function(String placementId)? onClick,
    Function(String placementId)? onComplete,
    Function(String placementId)? onRewarded,
    Function(String placementId)? onAdLeftApp,
    Function(String placementId)? onAdViewed,
    Function(String placementId, VungleError error, String errorMessage)?
    onFailed,
  }) async {
    _adChannels
        .putIfAbsent(placementId, () => _AdMethodChannel(placementId))
        .update(
      onAdStart: onStart,
      onAdClick: onClick,
      onAdComplete: onComplete,
      onAdRewarded: onRewarded,
      onAdLeftApp: onAdLeftApp,
      onAdViewed: onAdViewed,
      onShowFailed: onFailed,
    );

    final arguments = <String, dynamic>{
      PLACEMENT_ID_PARAMETER: placementId,
    };
    await _channel.invokeMethod(PLAY_METHOD, arguments);
  }

  /// Check if ad playable by a [placementId]
  ///
  /// Sometimes, you may not care when an ad is ready to play, you just care if there is any available ads when you want to show them.
  /// You can use following code to do this:
  /// ```dart
  /// if(await Vungle.isAdPlayable('<placementId>')) {
  ///   Vungle.playAd('<placementId>');
  /// }
  /// ```
  static Future<bool> isAdPlayable(String placementId) async {
    final bool isAdAvailable =
        await _channel.invokeMethod(CAN_PLAY_METHOD, <String, dynamic>{
          PLACEMENT_ID_PARAMETER: placementId,
    }) ?? false;
    return isAdAvailable;
  }

  /// Update Consent Status
  ///
  /// For GDPR users, you may need show a consent dialog to them, and you need call this method to pass the user's decision to the SDK,
  /// "Accepted" or "Denied". That SDK could follow the GDPR policy correctly.
  static void updateConsentStatus(
      UserConsentStatus status, String consentMessageVersion) {
    _channel.invokeMethod('updateConsentStatus', <String, dynamic>{
      'consentStatus': status.toString(),
      'consentMessageVersion': consentMessageVersion,
    });
  }

  /// Get Consent Status
  static Future<UserConsentStatus> getConsentStatus() async {
    final String status = await _channel.invokeMethod('getConsentStatus', <String, dynamic>{}) ?? '';
    return _statusStringToUserConsentStatus[status] ?? UserConsentStatus.Denied;
  }

  /// Get Consent Message version
  static Future<String> getConsentMessageVersion() async {
    final String version =
        await _channel.invokeMethod('getConsentMessageVersion', <String, dynamic>{}) ?? '';
    return version;
  }

  static const Map<String, UserConsentStatus> _statusStringToUserConsentStatus =
      {
    'Accepted': UserConsentStatus.Accepted,
    'Denied': UserConsentStatus.Denied,
  };
}

class _AdMethodChannel {
  final MethodChannel channel;
  Function(String placementId)? onLoadComplete;
  Function(String placementId, VungleError error, String errorMessage)?
  onLoadFailed;
  Function(String placementId)? onAdStart;
  Function(String placementId)? onAdClick;
  Function(String placementId)? onAdComplete;
  Function(String placementId)? onAdRewarded;
  Function(String placementId)? onAdLeftApp;
  Function(String placementId)? onAdViewed;
  Function(String placementId, VungleError error, String errorMessage)?
  onShowFailed;

  _AdMethodChannel(String placementId)
      : channel = MethodChannel('${VIDEO_AD_CHANNEL}_$placementId') {
    channel.setMethodCallHandler(_methodCallHandler);
  }

  void update({
    Function(String placementId)? onLoadComplete,
    Function(String placementId, VungleError error, String errorMessage)?
    onLoadFailed,
    Function(String placementId)? onAdStart,
    Function(String placementId)? onAdClick,
    Function(String placementId)? onAdComplete,
    Function(String placementId)? onAdRewarded,
    Function(String placementId)? onAdLeftApp,
    Function(String placementId)? onAdViewed,
    Function(String placementId, VungleError error, String errorMessage)?
    onShowFailed,
  }) {
    this.onLoadComplete = onLoadComplete ?? this.onLoadComplete;
    this.onLoadFailed = onLoadFailed ?? this.onLoadFailed;
    this.onAdStart = onAdStart ?? this.onAdStart;
    this.onAdClick = onAdClick ?? this.onAdClick;
    this.onAdComplete = onAdComplete ?? this.onAdComplete;
    this.onAdRewarded = onAdRewarded ?? this.onAdRewarded;
    this.onAdLeftApp = onAdLeftApp ?? this.onAdLeftApp;
    this.onAdViewed = onAdViewed ?? this.onAdViewed;
    this.onShowFailed = onShowFailed ?? this.onShowFailed;
  }

  Future _methodCallHandler(MethodCall call) async {
    switch (call.method) {
      case LOAD_COMPLETE_METHOD:
        onLoadComplete?.call(call.arguments[PLACEMENT_ID_PARAMETER]);
        break;
      case LOAD_FAILED_METHOD:
        onLoadFailed?.call(
          call.arguments[PLACEMENT_ID_PARAMETER],
          errorFromString(call.arguments[ERROR_CODE_PARAMETER]),
          call.arguments[ERROR_MESSAGE_PARAMETER],
        );
        break;
      case PLAY_START_METHOD:
        onAdStart?.call(call.arguments[PLACEMENT_ID_PARAMETER]);
        break;
      case PLAY_CLICK_METHOD:
        onAdClick?.call(call.arguments[PLACEMENT_ID_PARAMETER]);
        break;
      case PLAY_COMPLETE_METHOD:
        onAdComplete?.call(call.arguments[PLACEMENT_ID_PARAMETER]);
        break;
      case PLAY_REWARDED_METHOD:
        onAdRewarded?.call(call.arguments[PLACEMENT_ID_PARAMETER]);
        break;
      case PLAY_LEFT_APP_METHOD:
        onAdLeftApp?.call(call.arguments[PLACEMENT_ID_PARAMETER]);
        break;
      case PLAY_VIEWED_METHOD:
        onAdViewed?.call(call.arguments[PLACEMENT_ID_PARAMETER]);
        break;
      case PLAY_FAILED_METHOD:
        onShowFailed?.call(
          call.arguments[PLACEMENT_ID_PARAMETER],
          errorFromString(call.arguments[ERROR_CODE_PARAMETER]),
          call.arguments[ERROR_MESSAGE_PARAMETER],
        );
        break;
    }
  }
}
