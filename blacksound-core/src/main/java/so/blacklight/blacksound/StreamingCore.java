package so.blacklight.blacksound;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import so.blacklight.blacksound.spotify.SpotifyConfig;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class StreamingCore {

    private final SpotifyApi spotifyApi;

    public StreamingCore(final SpotifyConfig config) {
        final var uri = SpotifyHttpManager.makeUri(config.getRedirectUri().toString());

        spotifyApi = config.setupSecrets(new SpotifyApi.Builder())
                .setRedirectUri(config.getRedirectUri())
                .build();

    }

    public URI requestAuthorisationURI() {
        // Not sure if this needs to be more dynamic
        final var scopeItems = new String[] {
                "user-read-playback-state",
                "streaming",
                "playlist-read-collaborative",
                "user-modify-playback-state",
                "user-read-currently-playing"
        };

        final var scope = String.join(" ", scopeItems);
        final var authCodeUriRequest = spotifyApi.authorizationCodeUri()
                .scope(scope)
                .build();

        // Maybe we should consider caching the URI instead of recalculating it every time
        return authCodeUriRequest.execute();
    }

    public CompletableFuture<AuthorizationCodeCredentials> requestAuthorisation(final String code) {
        final var authCodeRequest = spotifyApi.authorizationCode(code).build();

        return authCodeRequest.executeAsync();
    }
}
