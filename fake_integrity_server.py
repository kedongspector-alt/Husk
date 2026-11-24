#!/usr/bin/env python3
"""
Fake Play Integrity server for testing apps running under Husk.

Two endpoints:
  POST /integrity  -- what Husk's HuskIntegrityManager calls to get a "token".
                      Returns {"token": "..."} which the app then ships to its
                      own backend unchanged.
  POST /decode     -- what YOUR app's backend would normally hit on Google
                      (playintegrity.googleapis.com/.../decodeIntegrityToken).
                      Point your backend's PI_URL env var here and it gets back
                      the structure Google would return, with whatever verdict
                      you set below.

Flip VERDICT to walk through every path your app handles.

Run:  python3 fake_integrity_server.py 8080
"""
from http.server import BaseHTTPRequestHandler, HTTPServer
import json, sys

# Change this to test each code path in your app.
#   []                                                      -> device totally failed
#   ["MEETS_BASIC_INTEGRITY"]                               -> emulator / rooted-ish
#   ["MEETS_BASIC_INTEGRITY", "MEETS_DEVICE_INTEGRITY"]     -> pass case (DEVICE)
#   [...above..., "MEETS_STRONG_INTEGRITY"]                 -> strong
VERDICT = ["MEETS_BASIC_INTEGRITY", "MEETS_DEVICE_INTEGRITY"]

def make_payload(verdicts, nonce="stub", package="com.yourapp"):
    return {
        "tokenPayloadExternal": {
            "requestDetails": {
                "requestPackageName": package,
                "nonce": nonce,
                "timestampMillis": "1700000000000",
            },
            "appIntegrity": {
                "appRecognitionVerdict": "PLAY_RECOGNIZED",
                "packageName": package,
                "certificateSha256Digest": ["stub"],
                "versionCode": "1",
            },
            "deviceIntegrity": {"deviceRecognitionVerdict": verdicts},
            "accountDetails": {"appLicensingVerdict": "LICENSED"},
        }
    }

class H(BaseHTTPRequestHandler):
    def _read_body(self):
        n = int(self.headers.get("Content-Length") or 0)
        if n <= 0: return {}
        try: return json.loads(self.rfile.read(n).decode("utf-8"))
        except Exception: return {}

    def _send(self, obj, status=200):
        body = json.dumps(obj).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_POST(self):
        req = self._read_body()
        print(f"[{self.path}] <- {req}", flush=True)
        if self.path.startswith("/integrity"):
            # Called by Husk's HuskIntegrityManager. Return any non-empty token.
            # The token is just a string the app forwards to its own backend.
            nonce = req.get("nonce", "")
            self._send({"token": f"husk-{nonce or 'stub'}"})
        elif self.path.startswith("/decode"):
            # Called by YOUR app's backend. Return the full PI decode shape.
            self._send(make_payload(VERDICT))
        else:
            self._send({"error": "unknown path"}, 404)

    def log_message(self, fmt, *args): pass  # quiet

if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8080
    print(f"Fake Play Integrity server on :{port}  VERDICT={VERDICT}", flush=True)
    HTTPServer(("0.0.0.0", port), H).serve_forever()
