package com.google.android.play.core.integrity;

public final class IntegrityTokenRequest {
    private final String nonce;
    private final Long cloudProjectNumber;

    private IntegrityTokenRequest(String nonce, Long cpn) {
        this.nonce = nonce;
        this.cloudProjectNumber = cpn;
    }

    public String nonce() { return nonce; }
    public Long cloudProjectNumber() { return cloudProjectNumber; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String nonce;
        private Long cloudProjectNumber;
        public Builder setNonce(String nonce) { this.nonce = nonce; return this; }
        public Builder setCloudProjectNumber(long n) { this.cloudProjectNumber = n; return this; }
        public IntegrityTokenRequest build() { return new IntegrityTokenRequest(nonce, cloudProjectNumber); }
    }
}
