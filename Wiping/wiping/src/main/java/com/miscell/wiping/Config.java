package com.miscell.wiping;

import android.content.Context;
import com.miscell.wiping.utils.PreferenceUtil;

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
}
