import java.io.IOException;
import java.nio.file.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class CertGenerator {
    public static void main(String[] args) throws Exception {
        Path publicPath = Paths.get("public.key");
        Path privatePath = Paths.get("private.key");
        if (Files.exists(publicPath) || Files.exists(privatePath)) {
            System.out.println("Key file already exists. Refusing to overwrite it.");
            System.out.println("Delete BOTH key files first only if you intentionally want a new pair.");
            return;
        }
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        Files.write(publicPath, pair.getPublic().getEncoded(), StandardOpenOption.CREATE_NEW);
        try {
            Files.write(privatePath, pair.getPrivate().getEncoded(), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            Files.deleteIfExists(publicPath);
            throw e;
        }
        System.out.println("DIGIGUARD RSA-2048 key pair generated locally.");
        System.out.println("Keep private.key secret. Never upload it to public GitHub.");
    }
}
