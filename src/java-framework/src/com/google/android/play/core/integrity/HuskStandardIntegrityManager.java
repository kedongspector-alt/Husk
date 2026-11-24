package com.google.android.play.core.integrity;

import android.content.Context;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

/** Same idea as HuskIntegrityManager but for the Standard API shape. Returns a
 *  provider whose request() always resolves to a canned token string. */
final class HuskStandardIntegrityManager implements StandardIntegrityManager {
    private final Context ctx;
    HuskStandardIntegrityManager(Context ctx) { this.ctx = ctx; }

    @Override
    public Task<StandardIntegrityTokenProvider> prepareIntegrityToken(PrepareIntegrityTokenRequest req) {
        TaskCompletionSource<StandardIntegrityTokenProvider> src = new TaskCompletionSource<>();
        src.setResult(new StandardIntegrityTokenProvider() {
            @Override public Task<StandardIntegrityToken> request(StandardIntegrityTokenRequest r) {
                TaskCompletionSource<StandardIntegrityToken> inner = new TaskCompletionSource<>();
                inner.setResult(StandardIntegrityToken.of("husk-stub-token"));
                return inner.getTask();
            }
        });
        return src.getTask();
    }
}
