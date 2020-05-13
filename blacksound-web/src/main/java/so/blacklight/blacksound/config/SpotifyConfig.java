package so.blacklight.blacksound.config;

import java.net.URI;

public class SpotifyConfig {

    private String clientId;

    private String clientSecret;

    private URI redirectUri;

    // The package-private constructor is reserved for serialisation/de-serialisation and should not be used normally
    SpotifyConfig() {
        clientId = "";
        clientSecret = "";
        redirectUri = URI.create("http://localhost");
    }

    public SpotifyConfig(final String clientId, final String clientSecret, final String redirectUri) {
        this(clientId, clientSecret, URI.create(redirectUri));
    }

    public SpotifyConfig(final String clientId, final String clientSecret, final URI redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public URI getRedirectUri() {
        return redirectUri;
    }
}
