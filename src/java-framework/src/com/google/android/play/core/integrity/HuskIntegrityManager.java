package com.google.android.play.core.integrity;

import android.content.Context;
import android.os.SystemProperties;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Husk's Play Integrity stand-in. Instead of binding to Google Play Services, it
 *  POSTs the nonce to a URL you control and returns whatever token that server hands
 *  back. The URL comes from the "husk.integrity.url" system property (or the
 *  compile-time default), so it's switchable without rebuilding.
 *
 *  If no URL is set, returns a canned non-empty token string so the app's call
 *  succeeds and it ships that string to its own backend unchanged. */
final class HuskIntegrityManager implements IntegrityManager {
    /** Change this to your server's address, or set husk.integrity.url at runtime. */
    private static final String DEFAULT_URL = "http://192.168.1.137:8080/integrity";
    /** Canned token returned when no server is configured. Any non-empty string works
     *  — the app only inspects the verdict after its backend decodes it, which in your
     *  test setup you've redirected to a server you control. */
    private static final String FALLBACK_TOKEN = "husk-stub-token";

    private final Context ctx;
    HuskIntegrityManager(Context ctx) { this.ctx = ctx; }

    @Override
    public Task<IntegrityTokenResponse> requestIntegrityToken(IntegrityTokenRequest request) {
        TaskCompletionSource<IntegrityTokenResponse> src = new TaskCompletionSource<>();
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    String url = SystemProperties.get("husk.integrity.url", DEFAULT_URL);
                    String token = (url == null || url.isEmpty()) ? FALLBACK_TOKEN : fetch(url, request);
                    src.setResult(IntegrityTokenResponse.of(token));
                } catch (Throwable t) {
                    // Even on network error, return the fallback — never fail the Task,
                    // or the app's onFailureListener will refuse to run.
                    src.setResult(IntegrityTokenResponse.of(FALLBACK_TOKEN));
                }
            }
        }, "husk-integrity").start();
        return src.getTask();
    }

    private static String fetch(String url, IntegrityTokenRequest req) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        try {
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/json");
            c.setConnectTimeout(5000);
            c.setReadTimeout(10000);
            String nonce = req == null || req.nonce() == null ? "" : req.nonce();
            Long cpn = req == null ? null : req.cloudProjectNumber();
            String body = "{\"nonce\":\"" + escape(nonce) + "\",\"cloudProjectNumber\":" + (cpn == null ? "null" : cpn) + "}";
            try (OutputStream os = c.getOutputStream()) { os.write(body.getBytes(StandardCharsets.UTF_8)); }
            if (c.getResponseCode() != 200) return FALLBACK_TOKEN;
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
                String line; while ((line = r.readLine()) != null) sb.append(line);
            }
            // Expect {"token":"..."} — very small parser so we don't pull in org.json at boot.
            String s = sb.toString();
            int i = s.indexOf("\"token\"");
            if (i < 0) return s.isEmpty() ? FALLBACK_TOKEN : s;
            int q1 = s.indexOf('"', s.indexOf(':', i) + 1);
            int q2 = s.indexOf('"', q1 + 1);
            return q1 < 0 || q2 < 0 ? FALLBACK_TOKEN : s.substring(q1 + 1, q2);
        } finally {
            c.disconnect();
        }
    }

    private static String escape(String s) {
        StringBuilder b = new StringBuilder(s.length() + 2);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '"' || ch == '\\') b.append('\\').append(ch);
            else if (ch < 0x20) b.append(String.format("\\u%04x", (int) ch));
            else b.append(ch);
        }
        return b.toString();
    }
}
