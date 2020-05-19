package so.blacklight.blacksound.spotify;

import com.wrapper.spotify.SpotifyApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Spotify Config")
public class SpotifyConfigTest {

    @Test
    @DisplayName("Redirect URI is populated via constructor arguments")
    void redirectUriIsPopulated() {
        final var redirectUri = URI.create("http://test-uri");
        final var config = new SpotifyConfig("ClientID", "ClientSecret", redirectUri);

        assertNotNull(config.getRedirectUri());
        assertEquals(redirectUri, config.getRedirectUri());
    }

    @Test
    @DisplayName("Client secrets are populated on the Spotify API")
    void secretsArePopulated() {
        final var clientId = "ClientID";
        final var clientSecret = "ClientSecret";

        final var clientIdSet = new AtomicBoolean(false);
        final var clientSecretSet = new AtomicBoolean(false);

        final var builder = new SpotifyApi.Builder() {
            @Override
            public SpotifyApi.Builder setClientId(String clientId) {
                clientIdSet.set(true);
                return super.setClientId(clientId);
            }

            @Override
            public SpotifyApi.Builder setClientSecret(String clientSecret) {
                clientSecretSet.set(true);
                return super.setClientSecret(clientSecret);
            }
        };

        new SpotifyConfig(clientId, clientSecret, URI.create("http://localhost")).setupSecrets(builder);

        assertTrue(clientIdSet.get());
        assertTrue(clientSecretSet.get());
    }
}
