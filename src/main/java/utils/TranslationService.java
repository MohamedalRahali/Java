package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import java.io.InputStream;

public class TranslationService {
    private static final String RAPID_API_URL = "https://text-translator2.p.rapidapi.com/translate";
    private static final String RAPID_API_KEY;
    static {
        String envKey = System.getenv("RAPID_API_KEY");
        if (envKey == null || envKey.isEmpty()) {
            System.err.println("[TranslationService] WARNING: RAPID_API_KEY environment variable is not set. Translation API calls may fail or be insecure.");
            RAPID_API_KEY = "YOUR_RAPID_API_KEY_HERE"; // fallback dummy value
        } else {
            RAPID_API_KEY = envKey;
        }
    }
    private JSONObject translations;

    public TranslationService() {
        loadTranslations();
        // Test de traduction
        String testResult = translateText("Hello World!", "fr");
        System.out.println("Translation test - Hello World! -> " + testResult);
    }

    private void loadTranslations() {
        try {
            InputStream is = getClass().getResourceAsStream("/translations.json");
            if (is == null) {
                System.err.println("Fichier de traductions non trouvé");
                translations = new JSONObject();
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            translations = new JSONObject(content.toString());
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des traductions: " + e.getMessage());
            translations = new JSONObject();
        }
    }

    public String translateText(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // Déterminer la paire de langues
        String sourceLang = "fr";
        if (targetLanguage.equals("fr")) {
            sourceLang = "en";
        }
        String langPair = sourceLang + "_" + targetLanguage;

        // Chercher dans le fichier de traductions
        if (translations.has(langPair)) {
            JSONObject langTranslations = translations.getJSONObject(langPair);
            if (langTranslations.has(text)) {
                return langTranslations.getString(text);
            }
        }

        // Si la traduction n'est pas trouvée, utiliser l'API
        try {
            URL url = new URL(RAPID_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("x-rapidapi-host", "text-translator2.p.rapidapi.com");
            conn.setRequestProperty("x-rapidapi-key", RAPID_API_KEY);
            conn.setDoOutput(true);

            // Préparer les données du formulaire
            String postData = String.format("source_language=%s&target_language=%s&text=%s",
                sourceLang, targetLanguage, text);

            // Envoyer les données
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 403) {
                System.err.println("[TranslationService] ERROR: HTTP 403 Forbidden. Check your RAPID_API_KEY and RapidAPI plan.");
                return text;
            } else if (responseCode == 429) {
                System.err.println("[TranslationService] ERROR: HTTP 429 Too Many Requests. You have hit the API rate limit. Try again later or reduce request frequency.");
                return text;
            }

            // Lire la réponse
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // Parser la réponse JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject data = jsonResponse.getJSONObject("data");
                return data.getString("translatedText");
            }

        } catch (Exception e) {
            System.err.println("Erreur pendant la traduction: " + e.getMessage());
            return text;
        }
    }

    public static String[] getAvailableLanguages() {
        return new String[] {
            "fr", // Français
            "en"  // Anglais
        };
    }
}