package com.google.android.play.core.integrity;

/** The app reads this via response.token() and ships the string to its own backend,
 *  which (in your test setup) is pointed at a server you control. */
public abstract class IntegrityTokenResponse {
    public abstract String token();

    static IntegrityTokenResponse of(final String token) {
        return new IntegrityTokenResponse() {
            @Override public String token() { return token; }
        };
    }
}
