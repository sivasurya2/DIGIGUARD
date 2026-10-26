# DIGIGUARD — RSA Challenge-Response Verification

A local Java + HTML college demonstration of RSA-2048 challenge-response signature verification.

## Requirements
- JDK 17 or newer (JDK 8+ may work, but JDK 17 is recommended).
- Windows instructions below; the Java source is platform-independent.

## First run
1. Extract the ZIP into a normal folder.
2. Run `run-windows.bat`. It compiles the Java files and generates a local RSA key pair only when both key files are absent.
3. Keep the terminal open and run `java Server` in that folder. (If the launcher has returned to its prompt, open a terminal in the folder and run it manually.)
4. Open a second terminal in the same folder and run `java WebServer`.
5. Open `index.html` in your browser and click **VERIFY CLIENT IDENTITY**.
6. Click **RUN NEGATIVE TEST — INVALID SIGNATURE** to send a deliberately corrupted signature; the backend should return FAIL.

The HTTP endpoint is `http://localhost:8080/auth`. The TCP authentication service listens on port 5000. The browser UI calls the HTTP endpoint; the standalone `Client.java` is an optional separate test client.

## Keys and trust model
- `CertGenerator.java` creates a matching RSA-2048 `public.key` and `private.key` pair locally.
- The server loads `public.key` as the one trusted key. The signing service loads `private.key`.
- Never commit `private.key` to GitHub. `.gitignore` excludes it and compiled `.class` files.
- If the pair is lost or mismatched, stop both servers, back up and remove BOTH key files, then run `java CertGenerator` to create a new pair. Do not regenerate keys while a server is running.

## Test scenarios
- Valid signature: expect `SUCCESS`.
- Negative test: expect `FAIL` and the red rejection state.
- Stop `Server` or `WebServer`: expect a connection error, never a false success.
- The server rejects public keys that do not match the configured trusted key.

## Scope and limitations
This is a local educational demonstration, not a production certificate authority or a secure remote identity service. The private key is used by the local Java WebServer, so the UI's “client-side” wording is only conceptual for this demo. The trusted key is configured from a local file. Real deployments require a managed trust registry/certificate validation, secure key storage, transport security, authentication/authorization policy, auditing, and security review. The HTTP server binds to 127.0.0.1 and is intended for local use only.

## Troubleshooting
- `Address already in use`: stop an older server process or change the port consistently in both Java files and the HTML.
- `SERVER_DOWN`: make sure both `java Server` and `java WebServer` are running from the project folder and that the key pair exists.
- Browser does not connect: try opening `index.html` directly in a current browser and check the Java terminal output.
- If the key files do not match, regenerate both together as described above.
