package com.google.android.play.core.integrity;

import com.google.android.gms.tasks.Task;

/** Stub for the Standard Integrity API (newer, token-provider based).
 *  Many apps still use the classic API, but we stub this too so prepareIntegrityToken
 *  calls don't crash. */
public interface StandardIntegrityManager {
    Task<StandardIntegrityTokenProvider> prepareIntegrityToken(PrepareIntegrityTokenRequest request);

    interface StandardIntegrityTokenProvider {
        Task<StandardIntegrityToken> request(StandardIntegrityTokenRequest request);
    }

    abstract class StandardIntegrityToken {
        public abstract String token();
        static StandardIntegrityToken of(final String t) {
            return new StandardIntegrityToken() { @Override public String token() { return t; } };
        }
    }

    final class PrepareIntegrityTokenRequest {
        private final Long cloudProjectNumber;
        private PrepareIntegrityTokenRequest(Long n) { this.cloudProjectNumber = n; }
        public Long cloudProjectNumber() { return cloudProjectNumber; }
        public static Builder builder() { return new Builder(); }
        public static final class Builder {
            private Long n;
            public Builder setCloudProjectNumber(long x) { this.n = x; return this; }
            public PrepareIntegrityTokenRequest build() { return new PrepareIntegrityTokenRequest(n); }
        }
    }

    final class StandardIntegrityTokenRequest {
        private final String requestHash;
        private StandardIntegrityTokenRequest(String h) { this.requestHash = h; }
        public String requestHash() { return requestHash; }
        public static Builder builder() { return new Builder(); }
        public static final class Builder {
            private String h;
            public Builder setRequestHash(String x) { this.h = x; return this; }
            public StandardIntegrityTokenRequest build() { return new StandardIntegrityTokenRequest(h); }
        }
    }
}
