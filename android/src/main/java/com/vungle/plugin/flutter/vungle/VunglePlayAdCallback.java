package com.vungle.plugin.flutter.vungle;

import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.error.VungleException;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;

public class VunglePlayAdCallback implements PlayAdCallback {
    private final Map<String, MethodChannel> placementChannels;
    private final BinaryMessenger messenger;

    public VunglePlayAdCallback(Map<String, MethodChannel> placementChannels, BinaryMessenger messenger) {
        this.placementChannels = placementChannels;
        this.messenger = messenger;
    }

    @Override
    public void creativeId(String creativeId) {
    }

    @Override
    public void onAdStart(String id) {
        invokeMethod(VungleConstants.PLAY_START_METHOD, id, new HashMap<String, String>());
    }

    @Deprecated
    @Override
    public void onAdEnd(String id, boolean completed, boolean isCTAClicked) {
    }

    @Override
    public void onAdEnd(String id) {
        invokeMethod(VungleConstants.PLAY_COMPLETE_METHOD, id, new HashMap<String, String>());
    }

    @Override
    public void onAdClick(String id) {
        invokeMethod(VungleConstants.PLAY_CLICK_METHOD, id, new HashMap<String, String>());
    }

    @Override
    public void onAdRewarded(String id) {
        invokeMethod(VungleConstants.PLAY_REWARDED_METHOD, id, new HashMap<String, String>());
    }

    @Override
    public void onAdLeftApplication(String id) {
        invokeMethod(VungleConstants.PLAY_LEFT_APP_METHOD, id, new HashMap<String, String>());
    }

    @Override
    public void onError(String id, VungleException exception) {
        Map<String, String> arguments = new HashMap<>();
        arguments.put(VungleConstants.ERROR_CODE_PARAMETER, VungleUtils.convertError(exception));
        arguments.put(VungleConstants.ERROR_MESSAGE_PARAMETER, exception.getLocalizedMessage());
        invokeMethod(VungleConstants.PLAY_FAILED_METHOD, id, arguments);
    }

    @Override
    public void onAdViewed(String id) {
        invokeMethod(VungleConstants.PLAY_VIEWED_METHOD, id, new HashMap<String, String>());
    }

    private void invokeMethod(String methodName, String placementId, Map<String, String> arguments) {
        arguments.put(VungleConstants.PLACEMENT_ID_PARAMETER, placementId);
        MethodChannel channel = placementChannels.get(placementId);
        if (channel == null) {
            channel = new MethodChannel(messenger, VungleConstants.VIDEO_AD_CHANNEL + "_" + placementId);
            placementChannels.put(placementId, channel);
        }
        channel.invokeMethod(methodName, arguments);
    }
}
