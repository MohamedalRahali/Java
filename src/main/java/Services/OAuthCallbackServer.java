package Services;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.HashMap;

public class OAuthCallbackServer {
    private final HttpServer server;
    private final CompletableFuture<String> authCodeFuture;
    private final int port;

    public OAuthCallbackServer() throws IOException {
        authCodeFuture = new CompletableFuture<>();
        
        // Try ports from 8080 to 8090
        HttpServer tempServer = null;
        int tempPort = 8080;
        IOException lastException = null;
        
        while (tempPort <= 8090) {
            try {
                tempServer = HttpServer.create(new InetSocketAddress(tempPort), 0);
                break;
            } catch (IOException e) {
                lastException = e;
                tempPort++;
            }
        }
        
        if (tempServer == null) {
            throw new IOException("Could not find available port between 8080 and 8090", lastException);
        }
        
        this.port = tempPort;
        this.server = tempServer;
        server.createContext("/callback", new CallbackHandler());
        server.setExecutor(null);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public int getPort() {
        return port;
    }

    public CompletableFuture<String> getAuthorizationCode() {
        return authCodeFuture;
    }

    private class CallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();
            Map<String, String> params = parseQueryString(query);
            
            String response;
            if (params.containsKey("code")) {
                String code = params.get("code");
                authCodeFuture.complete(code);
                response = """
                    <html>
                    <head>
                        <title>Authentication Successful</title>
                        <style>
                            body { font-family: Arial, sans-serif; text-align: center; padding-top: 50px; background-color: #f0f0f0; }
                            .success { color: #4CAF50; }
                            .container { background-color: white; max-width: 500px; margin: 0 auto; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                        </style>
                        <script>
                            setTimeout(function() {
                                window.close();
                            }, 3000);
                        </script>
                    </head>
                    <body>
                        <div class="container">
                            <h1 class="success">✓ Authentication Successful!</h1>
                            <p>You have successfully signed in with Google.</p>
                            <p>This window will close automatically in 3 seconds...</p>
                        </div>
                    </body>
                    </html>
                    """;
            } else {
                String error = params.getOrDefault("error", "Unknown error");
                authCodeFuture.completeExceptionally(new RuntimeException("Authentication failed: " + error));
                response = """
                    <html>
                    <head>
                        <title>Authentication Failed</title>
                        <style>
                            body { font-family: Arial, sans-serif; text-align: center; padding-top: 50px; background-color: #f0f0f0; }
                            .error { color: #f44336; }
                            .container { background-color: white; max-width: 500px; margin: 0 auto; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h1 class="error">✗ Authentication Failed</h1>
                            <p>Error: %s</p>
                            <p>You can close this window and try again.</p>
                        </div>
                    </body>
                    </html>
                    """.formatted(error);
            }

            byte[] responseBytes = response.getBytes();
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }

        private Map<String, String> parseQueryString(String query) {
            Map<String, String> params = new HashMap<>();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        params.put(pair[0], pair[1]);
                    }
                }
            }
            return params;
        }
    }
} 