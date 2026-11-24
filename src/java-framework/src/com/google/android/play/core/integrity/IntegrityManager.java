package com.google.android.play.core.integrity;

import com.google.android.gms.tasks.Task;

/** Husk stub of the Play Integrity client library's main entry point. Apps call
 *  IntegrityManagerFactory.create(context).requestIntegrityToken(request), then wait
 *  on the returned Task. Our implementation lives in HuskIntegrityManager and hits a
 *  configurable HTTP endpoint instead of the real Play Services binder. */
public interface IntegrityManager {
    Task<IntegrityTokenResponse> requestIntegrityToken(IntegrityTokenRequest request);
}
