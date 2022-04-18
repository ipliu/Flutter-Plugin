import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'ad_instance_manager.dart';
import 'ad_listeners.dart';

/// Error information about why an ad operation failed.
class VungleException {
  /// Creates an [VungleException] with the given [code] and [message].
  @protected
  VungleException(this.code, this.message);

  /// Unique code to identify the error.
  final int code;

  /// A message detailing the error.
  final String message;

  @override
  String toString() {
    return '$runtimeType(code: $code, message: $message)';
  }
}

/// [AdSize] represents the size of an ad.
class AdSize {
  /// Constructs an [AdSize] with the given [name], [width] and [height].
  const AdSize({
    required this.name,
    required this.width,
    required this.height,
  });

  /// The size name.
  final String name;

  /// The horizontal span of an ad.
  final int width;

  /// The vertical span of an ad.
  final int height;

  /// The default ad has no size.
  static const AdSize vungleDefault = AdSize(name: 'default', width: -1, height: -1);

  /// The mrec ad (300x250) size.
  static const AdSize vungleMrec = AdSize(name: 'mrec', width: 300, height: 250);

  /// The standard banner (320x50) size.
  static const AdSize banner = AdSize(name: 'banner', width: 320, height: 50);

  /// The short banner (300x50) size.
  static const AdSize bannerShort = AdSize(name: 'banner_short', width: 300, height: 50);

  /// The leaderboard banner (728x90) size.
  static const AdSize bannerLeaderboard = AdSize(name: 'banner_leaderboard', width: 728, height: 90);

  @override
  bool operator ==(Object other) {
    return other is AdSize
        && name == other.name
        && width == other.width
        && height == other.height;
  }
}

/// The base class for all ads.
///
/// A valid [placementId] is required.
abstract class Ad {
  /// Default constructor, used by subclasses.
  Ad({required this.placementId});

  /// Identifies the source of [Ad]s for your application.
  final String placementId;

  /// Frees the plugin resources associated with this ad.
  Future<void> dispose() {
    return instanceManager.disposeAd(this);
  }
}

/// Base class for mobile [Ad] that has an in-line view.
///
/// A valid [placementId] and [size] are required.
abstract class AdWithView extends Ad {
  /// Default constructor, used by subclasses.
  AdWithView({required String placementId, required this.listener})
      : super(placementId: placementId);

  /// The [AdWithViewListener] for the ad.
  final AdWithViewListener listener;

  /// Starts loading this ad.
  ///
  /// Loading callbacks are sent to this [Ad]'s [listener].
  Future<void> load();
}

/// Displays an [Ad] as a Flutter widget.
///
/// This widget takes ads inheriting from [AdWithView]
/// (e.g. [BannerAd]) and allows them to be added to the Flutter
/// widget tree.
///
/// Must call `load()` first before showing the widget. Otherwise, a
/// [PlatformException] will be thrown.
class AdWidget extends StatefulWidget {
  /// Default constructor for [AdWidget].
  ///
  /// [ad] must be loaded before this is added to the widget tree.
  const AdWidget({Key? key, required this.ad}) : super(key: key);

  /// Ad to be displayed as a widget.
  final AdWithView ad;

  @override
  _AdWidgetState createState() => _AdWidgetState();
}

class _AdWidgetState extends State<AdWidget> {
  bool _adIdAlreadyMounted = false;
  bool _adLoadNotCalled = false;

  @override
  void initState() {
    super.initState();
    final int? adId = instanceManager.adIdFor(widget.ad);
    if (adId != null) {
      if (instanceManager.isWidgetAdIdMounted(adId)) {
        _adIdAlreadyMounted = true;
      }
      instanceManager.mountWidgetAdId(adId);
    } else {
      _adLoadNotCalled = true;
    }
  }

  @override
  void dispose() {
    super.dispose();
    final int? adId = instanceManager.adIdFor(widget.ad);
    if (adId != null) {
      instanceManager.unmountWidgetAdId(adId);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_adIdAlreadyMounted) {
      throw FlutterError.fromParts(<DiagnosticsNode>[
        ErrorSummary('This AdWidget is already in the Widget tree'),
        ErrorHint(
            'If you placed this AdWidget in a list, make sure you create a new instance '
                'in the builder function with a unique ad object.'),
        ErrorHint(
            'Make sure you are not using the same ad object in more than one AdWidget.'),
      ]);
    }
    if (_adLoadNotCalled) {
      throw FlutterError.fromParts(<DiagnosticsNode>[
        ErrorSummary(
            'AdWidget requires Ad.load to be called before AdWidget is inserted into the tree'),
        ErrorHint(
            'Parameter ad is not loaded. Call Ad.load before AdWidget is inserted into the tree.'),
      ]);
    }
    if (defaultTargetPlatform == TargetPlatform.android) {
      AdSize? size;
      if (widget.ad is BannerAd) {
        size = (widget.ad as BannerAd).size;
      }
      return SizedBox(
        width: size?.width.toDouble(),
        height: size?.height.toDouble(),
        child: AndroidView(
          viewType: '${instanceManager.channel.name}/ad_widget',
          layoutDirection: TextDirection.ltr,
          creationParams: instanceManager.adIdFor(widget.ad),
          creationParamsCodec: StandardMessageCodec(),
        ),
      );
    }

    return UiKitView(
      viewType: '${instanceManager.channel.name}/ad_widget',
      creationParams: instanceManager.adIdFor(widget.ad),
      creationParamsCodec: StandardMessageCodec(),
    );
  }
}

/// A banner ad.
///
/// This ad can either be overlaid on top of all flutter widgets as a static
/// view or displayed as a typical Flutter widget. To display as a widget,
/// instantiate an [AdWidget] with this as a parameter.
class BannerAd extends AdWithView {
  /// Creates a [BannerAd].
  ///
  /// A valid [adUnitId], nonnull [listener], and nonnull request is required.
  BannerAd({
    required this.size,
    required String placementId,
    required this.listener,
  }) : super(placementId: placementId, listener: listener);

  /// Represents the size of a banner ad.
  final AdSize size;

  /// A listener for receiving events in the ad lifecycle.
  @override
  final BannerAdListener listener;

  @override
  Future<void> load() async {
    await instanceManager.loadBannerAd(this);
  }

  /// Returns the AdSize of the associated platform ad object.
  ///
  /// The dimensions of the [AdSize] returned here may differ from [size],
  /// depending on what type of [AdSize] was used.
  /// The future will resolve to null if [load] has not been called yet.
  Future<AdSize?> getPlatformAdSize() async {
    return await instanceManager.getAdSize(this);
  }
}
