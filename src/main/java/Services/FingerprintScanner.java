package Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class FingerprintScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(FingerprintScanner.class);
    private static final String IPHONE_API_URL = "http://192.168.1.106:8080/api/fingerprint/verify";

    public FingerprintScanner() {
        LOGGER.info("FingerprintScanner initialisé pour iPhone");
    }

    public boolean verifyFingerprint() {
        try {
            LOGGER.info("Début de la vérification d'empreinte digitale via iPhone");
            
            // Attendre la réponse de l'iPhone
            boolean verified = waitForIPhoneResponse();
            
            if (verified) {
                LOGGER.info("Empreinte digitale vérifiée avec succès via iPhone");
            } else {
                LOGGER.warn("Échec de la vérification d'empreinte digitale via iPhone");
            }
            
            return verified;
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la vérification d'empreinte: {}", e.getMessage());
            return false;
        }
    }

    private boolean waitForIPhoneResponse() {
        try {
            URL url = new URL(IPHONE_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); // 5 secondes timeout

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = in.readLine();
                in.close();
                return "true".equals(response);
            }
            return false;
        } catch (Exception e) {
            LOGGER.error("Erreur de communication avec l'iPhone: {}", e.getMessage());
            return false;
        }
    }
} 