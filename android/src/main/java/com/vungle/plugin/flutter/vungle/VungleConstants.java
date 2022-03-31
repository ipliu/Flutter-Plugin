package com.vungle.plugin.flutter.vungle;

public final class VungleConstants {
    public final static String MAIN_CHANNEL = "flutter_vungle";

    public final static String VIDEO_AD_CHANNEL = MAIN_CHANNEL + "/videoAd";

    public final static String PLACEMENT_ID_PARAMETER = "placementId";
    public final static String ERROR_CODE_PARAMETER = "errorCode";
    public final static String ERROR_MESSAGE_PARAMETER = "errorMessage";

    //initialize
    public final static String APP_ID_PARAMETER = "appId";
    public final static String INIT_METHOD = "init";
    public final static String INIT_COMPLETE_METHOD = "initComplete";
    public final static String INIT_FAILED_METHOD = "initFailed";
    public final static String INIT_AUTO_CACHE_AD_METHOD = "initAutoCacheAd";

    //load
    public final static String LOAD_METHOD = "loadAd";
    public final static String LOAD_COMPLETE_METHOD = "loadAdComplete";
    public final static String LOAD_FAILED_METHOD = "loadAdFailed";

    //play
    public final static String CAN_PLAY_METHOD = "canPlayAd";
    public final static String PLAY_METHOD = "playAd";
    public final static String PLAY_START_METHOD = "playAdStart";
    public final static String PLAY_COMPLETE_METHOD = "playAdComplete";
    public final static String PLAY_FAILED_METHOD = "playAdFailed";
    public final static String PLAY_CLICK_METHOD = "playAdClick";
    public final static String PLAY_REWARDED_METHOD = "playAdRewarded";
    public final static String PLAY_LEFT_APP_METHOD = "playAdLeftApp";
    public final static String PLAY_VIEWED_METHOD = "playAdViewed";
}
