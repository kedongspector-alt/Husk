package com.google.android.play.core.integrity;

import android.content.Context;

/** Entry point apps call. Returns our stub instead of the real Play Services-backed manager. */
public final class IntegrityManagerFactory {
    private IntegrityManagerFactory() {}
    public static IntegrityManager create(Context context) {
        return new HuskIntegrityManager(context);
    }
    public static StandardIntegrityManager createStandard(Context context) {
        return new HuskStandardIntegrityManager(context);
    }
}
