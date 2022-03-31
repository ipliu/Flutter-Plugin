package com.vungle.plugin.flutter.vungle;

import com.vungle.warren.InitCallback;
import com.vungle.warren.error.VungleException;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

public class VungleInitCallback implements InitCallback {
    private final MethodChannel channel;

    public VungleInitCallback(MethodChannel channel) {
        this.channel = channel;
    }

    @Override
    public void onSuccess() {
        channel.invokeMethod(VungleConstants.INIT_COMPLETE_METHOD, new HashMap<>());
    }

    @Override
    public void onError(VungleException exception) {
        Map<String, String> arguments = new HashMap<>();
        arguments.put(VungleConstants.ERROR_CODE_PARAMETER, VungleUtils.convertError(exception));
        arguments.put(VungleConstants.ERROR_MESSAGE_PARAMETER, exception.getLocalizedMessage());
        channel.invokeMethod(VungleConstants.INIT_FAILED_METHOD, arguments);
    }

    @Override
    public void onAutoCacheAdAvailable(String placementId) {
        Map<String, String> arguments = new HashMap<>();
        arguments.put(VungleConstants.PLACEMENT_ID_PARAMETER, placementId);
        channel.invokeMethod(VungleConstants.INIT_AUTO_CACHE_AD_METHOD, arguments);
    }
}
