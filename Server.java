import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;

public class Server {
    private static final int PORT = 5000;
    private static final int CHALLENGE_LENGTH = 32;
    private static final int RSA2048_SIGNATURE_LENGTH = 256;
    private static final int MAX_KEY_LENGTH = 4096;
    private static final int CLIENT_TIMEOUT_MS = 5000;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static void main(String[] args) throws Exception {
        byte[] trustedKeyBytes = Files.readAllBytes(Paths.get("public.key"));
        if (trustedKeyBytes.length == 0 || trustedKeyBytes.length > MAX_KEY_LENGTH)
            throw new IllegalStateException("public.key has an invalid length.");
        PublicKey trustedKey = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(trustedKeyBytes));
        if (!"RSA".equalsIgnoreCase(trustedKey.getAlgorithm()))
            throw new IllegalStateException("The trusted key must be RSA.");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("========================================");
            System.out.println(" DIGIGUARD authentication server");
            System.out.println(" Listening on port " + PORT);
            System.out.println("========================================");
            while (true) {
                Socket socket = serverSocket.accept();
                Thread worker = new Thread(() -> handle(socket, trustedKeyBytes, trustedKey));
                worker.setName("DIGIGUARD-client-handler");
                worker.start();
            }
        }
    }

    private static void handle(Socket socket, byte[] trustedKeyBytes, PublicKey trustedKey) {
        try (Socket client = socket;
             DataInputStream in = new DataInputStream(client.getInputStream());
             DataOutputStream out = new DataOutputStream(client.getOutputStream())) {
            client.setSoTimeout(CLIENT_TIMEOUT_MS);
            int keyLength = in.readInt();
            if (keyLength < 1 || keyLength > MAX_KEY_LENGTH) {
                sendRejected(out, "FAIL");
                return;
            }
            byte[] presentedKey = new byte[keyLength];
            in.readFully(presentedKey);
            if (!MessageDigest.isEqual(trustedKeyBytes, presentedKey)) {
                System.out.println("[REJECTED] Unregistered public key");
                sendRejected(out, "FAIL");
                return;
            }

            byte[] challenge = new byte[CHALLENGE_LENGTH];
            RANDOM.nextBytes(challenge);
            out.writeInt(challenge.length);
            out.write(challenge);
            out.flush();

            int signatureLength = in.readInt();
            if (signatureLength != RSA2048_SIGNATURE_LENGTH) {
                out.writeUTF("FAIL");
                out.flush();
                System.out.println("[FAIL] Invalid signature length");
                return;
            }
            byte[] signatureBytes = new byte[signatureLength];
            in.readFully(signatureBytes);

            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(trustedKey);
            verifier.update(challenge);
            boolean valid = verifier.verify(signatureBytes);
            String result = valid ? "SUCCESS" : "FAIL";
            System.out.println(valid ? "[SUCCESS] Trusted key signature verified" : "[FAIL] Invalid signature — access denied");
            out.writeUTF(result);
            out.flush();
        } catch (SocketTimeoutException e) {
            System.err.println("[TIMEOUT] Client did not complete authentication in time.");
        } catch (Exception e) {
            System.err.println("[ERROR] Authentication request failed: " + e.getMessage());
        }
    }

    // Negative challenge length signals rejection before a challenge is issued.
    private static void sendRejected(DataOutputStream out, String result) throws IOException {
        out.writeInt(-1);
        out.writeUTF(result);
        out.flush();
    }
}
