package com.vungle.plugin.flutter.vungle;

import android.content.Context;

import androidx.annotation.NonNull;

import com.vungle.warren.AdConfig;
import com.vungle.warren.BuildConfig;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** VunglePlugin */
public class VunglePlugin implements FlutterPlugin, MethodCallHandler {
  private Context context;
  private MethodChannel channel;
  private Map<String, MethodChannel> placementChannels;
  private BinaryMessenger binaryMessenger;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    channel = new MethodChannel(binding.getBinaryMessenger(), VungleConstants.MAIN_CHANNEL);
    channel.setMethodCallHandler(this);
    context = binding.getApplicationContext();
    binaryMessenger = binding.getBinaryMessenger();
    placementChannels = new HashMap<>();
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    Map<?, ?> arguments = (Map<?, ?>) call.arguments;

    switch (call.method) {
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
