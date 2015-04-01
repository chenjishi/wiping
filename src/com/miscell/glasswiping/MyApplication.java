package com.miscell.glasswiping;

import android.app.Application;
import com.flurry.android.FlurryAgent;
import com.miscell.glasswiping.utils.DirectoryUtils;
import com.miscell.glasswiping.utils.NetworkRequest;

/**
 * Created by chenjishi on 15/3/18.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DirectoryUtils.init(this);
        NetworkRequest.init(this);

        FlurryAgent.setLogEnabled(false);
        FlurryAgent.init(this, "VRBSH72C4PGCCFQ9QRS9");
    }
}
