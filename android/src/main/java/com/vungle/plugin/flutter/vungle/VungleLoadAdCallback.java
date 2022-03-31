package com.vungle.plugin.flutter.vungle;

import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.error.VungleException;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;

public class VungleLoadAdCallback implements LoadAdCallback {
    private final Map<String, MethodChannel> placementChannels;
    private final BinaryMessenger messenger;

    public VungleLoadAdCallback(Map<String, MethodChannel> placementChannels, BinaryMessenger messenger) {
        this.placementChannels = placementChannels;
        this.messenger = messenger;
    }

    @Override
    public void onAdLoad(String id) {
        invokeMethod(VungleConstants.LOAD_COMPLETE_METHOD, id, new HashMap<String, String>());
    }

    @Override
    public void onError(String id, VungleException exception) {
        Map<String, String> arguments = new HashMap<>();
        arguments.put(VungleConstants.ERROR_CODE_PARAMETER, VungleUtils.convertError(exception));
        arguments.put(VungleConstants.ERROR_MESSAGE_PARAMETER, exception.getLocalizedMessage());
        invokeMethod(VungleConstants.LOAD_FAILED_METHOD, id, arguments);
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
