import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;

public class Client {
    // Set to true to deliberately corrupt the signature for a negative test.
    static boolean simulateFailure = false;

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("localhost", 5000);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            socket.setSoTimeout(5000);
            byte[] publicKeyBytes = Files.readAllBytes(Paths.get("public.key"));
            out.writeInt(publicKeyBytes.length);
            out.write(publicKeyBytes);
            out.flush();

            int challengeLength = in.readInt();
            if (challengeLength == -1) {
                System.out.println("Authentication rejected: " + in.readUTF());
                return;
            }
            if (challengeLength < 1 || challengeLength > 4096)
                throw new IOException("Invalid challenge length from server.");
            byte[] challenge = new byte[challengeLength];
            in.readFully(challenge);

            byte[] keyBytes = Files.readAllBytes(Paths.get("private.key"));
            PrivateKey privateKey = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey);
            signer.update(challenge);
            byte[] signature = signer.sign();
            if (simulateFailure && signature.length > 0) {
                signature[0] ^= 0x01;
                System.out.println("Negative test: signature deliberately corrupted.");
            }
            out.writeInt(signature.length);
            out.write(signature);
            out.flush();
            System.out.println("Authentication result: " + in.readUTF());
        }
    }
}
