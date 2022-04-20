package com.vungle.plugin.flutter.vungle;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vungle.warren.AdConfig;
import com.vungle.warren.BuildConfig;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.StandardMethodCodec;

/** VunglePlugin */
public class VunglePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  private static final String TAG = "VunglePlugin";

  private static <T> T requireNonNull(T obj) {
    if (obj == null) {
      throw new IllegalArgumentException();
    }
    return obj;
  }

  // This is always null when not using v2 embedding.
  @Nullable private FlutterPluginBinding pluginBinding;
  @Nullable private AdInstanceManager instanceManager;
  @Nullable private AdMessageCodec adMessageCodec;

  private Context context;
  private MethodChannel channel;
  private Map<String, MethodChannel> placementChannels;
  private BinaryMessenger binaryMessenger;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    pluginBinding = binding;
    adMessageCodec = new AdMessageCodec(binding.getApplicationContext());
    channel = new MethodChannel(
            binding.getBinaryMessenger(),
            VungleConstants.MAIN_CHANNEL,
            new StandardMethodCodec(adMessageCodec));
    channel.setMethodCallHandler(this);
    instanceManager = new AdInstanceManager(channel);
    context = binding.getApplicationContext();
    binaryMessenger = binding.getBinaryMessenger();
    placementChannels = new HashMap<>();

    binding.getPlatformViewRegistry().registerViewFactory(
            "flutter_vungle/ad_widget", new VungleViewFactory(instanceManager));
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    if (instanceManager != null) {
      instanceManager.disposeAllAds();
    }
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    if (instanceManager != null) {
      instanceManager.setActivity(binding.getActivity());
    }
    if (adMessageCodec != null) {
      adMessageCodec.setContext(binding.getActivity());
    }
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    // Use the application context
    if (adMessageCodec != null && pluginBinding != null) {
      adMessageCodec.setContext(pluginBinding.getApplicationContext());
    }
    if (instanceManager != null) {
      instanceManager.setActivity(null);
    }
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    if (instanceManager != null) {
      instanceManager.setActivity(binding.getActivity());
    }
    if (adMessageCodec != null) {
      adMessageCodec.setContext(binding.getActivity());
    }
  }

  @Override
  public void onDetachedFromActivity() {
    if (adMessageCodec != null && pluginBinding != null) {
      adMessageCodec.setContext(pluginBinding.getApplicationContext());
    }
    if (instanceManager != null) {
      instanceManager.setActivity(null);
    }
  }

  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (instanceManager == null || pluginBinding == null) {
      Log.e(TAG, "method call received before instanceManager initialized: " + call.method);
      return;
    }
    Map<?, ?> arguments = (Map<?, ?>) call.arguments;

    switch (call.method) {
      case "_init":
        // Internal init. This is necessary to cleanup state on hot restart.
        instanceManager.disposeAllAds();
        result.success(null);
        break;
      case "getPlatformVersion":
        result.success("Android " + android.os.Build.VERSION.RELEASE);
        break;
      case VungleConstants.INIT_METHOD:
        result.success(init(arguments));
        break;
      case VungleConstants.LOAD_METHOD:
        result.success(loadAd(arguments));
        break;
      case VungleConstants.PLAY_METHOD:
        result.success(playAd(arguments));
        break;
      case VungleConstants.CAN_PLAY_METHOD:
        result.success(canPlayAd(arguments));
        break;
      case "updateConsentStatus":
        result.success(updateConsentStatus(arguments));
        break;
      case "getConsentStatus":
        result.success(getConsentStatus());
        break;
      case "getConsentMessageVersion":
        result.success(getConsentMessageVersion());
        break;
      case "sdkVersion":
        result.success(BuildConfig.VERSION_NAME);
        break;
      case "enableBackgroundDownload":
        // no op for this method.
        break;
      case "loadBannerAd":
        final FlutterBannerAd bannerAd =
                new FlutterBannerAd(
                        requireNonNull(call.<Integer>argument("adId")),
                        instanceManager,
                        requireNonNull(call.<String>argument("placementId")),
                        requireNonNull(call.<FlutterAdSize>argument("size")));
        instanceManager.trackAd(bannerAd, requireNonNull(call.<Integer>argument("adId")));
        bannerAd.load();
        result.success(null);
        break;
      case "disposeAd":
        instanceManager.disposeAd(requireNonNull(call.<Integer>argument("adId")));
        result.success(null);
        break;
      case "getAdSize":
        FlutterAd ad = instanceManager.adForId(requireNonNull(call.<Integer>argument("adId")));
        if (ad == null) {
          // This was called on a dart ad container that hasn't been loaded yet.
          result.success(null);
        } else if (ad instanceof FlutterBannerAd) {
          result.success(((FlutterBannerAd) ad).getAdSize());
        } else {
          result.error(
                  "unexpected_ad_type",
                  "Unexpected ad type for getAdSize: " + ad,
                  null);
        }
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  private boolean init(Map<?, ?> args) {
    String appId = (String) args.get(VungleConstants.APP_ID_PARAMETER);

    if (appId == null) {
      VungleException exception =
              new VungleException(VungleException.MISSING_REQUIRED_ARGUMENTS_FOR_INIT);
      Map<String, String> arguments = new HashMap<>();
      arguments.put(VungleConstants.ERROR_CODE_PARAMETER, VungleUtils.convertError(exception));
      arguments.put(VungleConstants.ERROR_MESSAGE_PARAMETER, exception.getLocalizedMessage());
      channel.invokeMethod(VungleConstants.INIT_FAILED_METHOD, arguments);
      return false;
    }

    Vungle.init(appId, this.context, new VungleInitCallback(channel));
    return true;
  }

  private boolean loadAd(Map<?, ?> args) {
    final String placementId = (String) args.get(VungleConstants.PLACEMENT_ID_PARAMETER);

    if(placementId == null) {
      return false;
    }

    Vungle.loadAd(placementId, new VungleLoadAdCallback(placementChannels, binaryMessenger));
    return true;
  }

  private boolean playAd(Map<?, ?> args) {
    final String placementId = (String) args.get(VungleConstants.PLACEMENT_ID_PARAMETER);

    if(placementId == null) {
      return false;
    }

    Vungle.playAd(placementId,
            new AdConfig(),
            new VunglePlayAdCallback(placementChannels, binaryMessenger));
    return true;
  }

  private boolean canPlayAd(Map<?, ?> args) {
    final String placementId = (String) args.get(VungleConstants.PLACEMENT_ID_PARAMETER);

    if(placementId == null) {
      return false;
    }

    return Vungle.canPlayAd(placementId);
  }

  private String getConsentStatus() {
    Vungle.Consent consent = Vungle.getConsentStatus();

    if (consent == null) {
      return "";
    }

    return VungleUtils.consentStatusToStr(consent);
  }

  private String getConsentMessageVersion() {
    return Vungle.getConsentMessageVersion();
  }

  private boolean updateConsentStatus(Map<?, ?> args) {
    final String consentStatus = (String) args.get("consentStatus");
    final String consentMessageVersion = (String) args.get("consentMessageVersion");

    if(consentStatus == null || consentMessageVersion == null) {
      return false;
    }

    Vungle.Consent consent = VungleUtils.strToConsentStatus(consentStatus);
    if(consent == null) {
      return false;
    }

    Vungle.updateConsentStatus(consent, consentMessageVersion);
    return true;
  }
}
