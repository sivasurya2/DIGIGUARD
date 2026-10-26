import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;

public class WebServer {
    private static final int HTTP_PORT = 8080;
    private static final int TCP_PORT = 5000;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", HTTP_PORT), 0);
        server.createContext("/auth", WebServer::handleAuth);
        server.start();
        System.out.println("DIGIGUARD WebServer running at http://localhost:8080/auth");
    }

    private static void handleAuth(HttpExchange exchange) throws IOException {
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        // Local college demo: permit file:// (null origin) and localhost browser origins only.
        if (origin == null || "null".equals(origin) || origin.startsWith("http://localhost") || origin.startsWith("http://127.0.0.1")) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin == null ? "*" : origin);
            exchange.getResponseHeaders().set("Vary", "Origin");
        } else {
            send(exchange, 403, "SERVER_DOWN");
            return;
        }
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "SERVER_DOWN");
            return;
        }
        String query = exchange.getRequestURI().getRawQuery();
        boolean negativeTest = query != null && query.matches("(?:.*&)?test=negative(?:&.*)?");
        send(exchange, 200, connect(negativeTest));
    }

    private static void send(HttpExchange exchange, int status, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) { out.write(bytes); }
    }

    public static String connect(boolean negativeTest) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", TCP_PORT), 3000);
            socket.setSoTimeout(5000);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            byte[] publicKeyBytes = Files.readAllBytes(Paths.get("public.key"));
            out.writeInt(publicKeyBytes.length);
            out.write(publicKeyBytes);
            out.flush();

            int challengeLength = in.readInt();
            if (challengeLength == -1) {
                return in.readUTF();
            }
            if (challengeLength < 1 || challengeLength > 4096) return "SERVER_DOWN";
            byte[] challenge = new byte[challengeLength];
            in.readFully(challenge);

            byte[] privateKeyBytes = Files.readAllBytes(Paths.get("private.key"));
            PrivateKey privateKey = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey);
            signer.update(challenge);
            byte[] signature = signer.sign();
            if (negativeTest && signature.length > 0) signature[0] ^= 0x01;

            out.writeInt(signature.length);
            out.write(signature);
            out.flush();
            String result = in.readUTF();
            return ("SUCCESS".equals(result) || "FAIL".equals(result)) ? result : "SERVER_DOWN";
        } catch (Exception e) {
            System.err.println("DIGIGUARD authentication request failed:");
            e.printStackTrace(System.err);
            return "SERVER_DOWN";
        }
    }
}
