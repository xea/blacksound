package so.blacklight.blacksound.spotify;

import com.wrapper.spotify.SpotifyApi;

import java.net.URI;

public class SpotifyConfig {

    private final String clientId;

    private final String clientSecret;

    private final URI redirectUri;

    // The package-private constructor is reserved for serialisation/de-serialisation and should not be used normally
    SpotifyConfig() {
        clientId = "";
        clientSecret = "";
        redirectUri = URI.create("http://localhost");
    }

    public SpotifyConfig(final String clientId, final String clientSecret, final URI redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public URI getRedirectUri() {
        return redirectUri;
    }

    public SpotifyApi.Builder setupSecrets(final SpotifyApi.Builder builder) {
        return builder.setClientId(clientId)
                .setClientSecret(clientSecret);
    }
}
