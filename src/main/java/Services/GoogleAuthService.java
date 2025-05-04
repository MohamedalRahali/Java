package Services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GoogleAuthService {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String CLIENT_ID = dotenv.get("GOOGLE_CLIENT_ID");
    private static final String CLIENT_SECRET = dotenv.get("GOOGLE_CLIENT_SECRET");
    private static final String REDIRECT_URI_BASE = dotenv.get("GOOGLE_REDIRECT_URI_BASE") != null ? dotenv.get("GOOGLE_REDIRECT_URI_BASE") : "http://localhost:%d/callback";
    private static final String APPLICATION_NAME = dotenv.get("GOOGLE_APP_NAME") != null ? dotenv.get("GOOGLE_APP_NAME") : "Wings Event Management";
    private static final List<String> SCOPES = Arrays.asList(
        "https://www.googleapis.com/auth/userinfo.profile",
        "https://www.googleapis.com/auth/userinfo.email"
    );

    private final GoogleAuthorizationCodeFlow flow;
    private final NetHttpTransport httpTransport;
    private final GsonFactory jsonFactory;
    private int port = 8080;

    public GoogleAuthService() throws IOException {
        this(8080);
    }

    public GoogleAuthService(int port) throws IOException {
        this.port = port;
        httpTransport = new NetHttpTransport();
        jsonFactory = GsonFactory.getDefaultInstance();

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets()
            .setWeb(new GoogleClientSecrets.Details()
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setRedirectUris(Arrays.asList(getRedirectUri())));

        flow = new GoogleAuthorizationCodeFlow.Builder(
            httpTransport,
            jsonFactory,
            clientSecrets,
            SCOPES
        ).build();
    }

    private String getRedirectUri() {
        return String.format(REDIRECT_URI_BASE, port);
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAuthorizationUrl() {
        return flow.newAuthorizationUrl()
            .setRedirectUri(getRedirectUri())
            .build();
    }

    public GoogleUserInfo handleCallback(String authCode) throws IOException {
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(authCode)
            .setRedirectUri(getRedirectUri())
            .execute();

        Credential credential = flow.createAndStoreCredential(tokenResponse, null);

        Oauth2 oauth2 = new Oauth2.Builder(
            httpTransport,
            jsonFactory,
            credential
        ).setApplicationName(APPLICATION_NAME)
         .build();

        Userinfo userInfo = oauth2.userinfo().get().execute();

        return new GoogleUserInfo(
            userInfo.getId(),
            userInfo.getEmail(),
            userInfo.getName(),
            userInfo.getPicture()
        );
    }

    public static class GoogleUserInfo {
        private final String id;
        private final String email;
        private final String name;
        private final String picture;

        public GoogleUserInfo(String id, String email, String name, String picture) {
            this.id = id;
            this.email = email;
            this.name = name;
            this.picture = picture;
        }

        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getPicture() { return picture; }
    }
} 