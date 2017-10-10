package com.softtim.mobilityconductor;

import android.content.Context;
import android.os.PowerManager;

/**
 * Created by softtim on 8/24/16.
 */
public  class WakeLocker {
    private static PowerManager.WakeLock wakeLock;

    public void acquire(Context ctx) {
        if (wakeLock != null) wakeLock.release();

        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "mytag");
        wakeLock.acquire();
    }

    public void release() {
        if (wakeLock != null) wakeLock.release(); wakeLock = null;
    }


}