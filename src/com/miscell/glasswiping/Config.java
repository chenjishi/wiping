package com.miscell.glasswiping;

import android.content.Context;
import com.miscell.glasswiping.utils.PreferenceUtil;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

/**
 * Created by chenjishi on 14/10/27.
 */
public class Config {
    private static final String KEY_ACESS_TOKEN = "access_token";
    private static final String KEY_EXPIRE_IN = "expire_in";

    private static final String KEY_BROWSE_MODE = "browse_mode";

    private Config() {

    }

    public static void saveBrowseMode(Context context, boolean b) {
        PreferenceUtil.putBoolean(context, KEY_BROWSE_MODE, b);
    }

    public static boolean getBrowseMode(Context context) {
        return PreferenceUtil.getBoolean(context, KEY_BROWSE_MODE, false);
    }

    public static void saveAccessToken(Context context, Oauth2AccessToken token) {
        PreferenceUtil.putString(context, KEY_ACESS_TOKEN, token.getToken());
        PreferenceUtil.putLong(context, KEY_EXPIRE_IN, token.getExpiresTime());
    }

    public static Oauth2AccessToken getAccessToken(Context context) {
        Oauth2AccessToken token = new Oauth2AccessToken();
        token.setToken(PreferenceUtil.getString(context, KEY_ACESS_TOKEN, ""));
        token.setExpiresTime(PreferenceUtil.getLong(context, KEY_EXPIRE_IN, 0L));
        return token;
    }
}
