package so.blacklight.blacksound;

import com.wrapper.spotify.SpotifyApi;
import so.blacklight.blacksound.spotify.SpotifyConfig;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class StreamingCore {

    private final SpotifyApi spotifyApi;
    private final SpotifyConfig config;

    public StreamingCore(final SpotifyConfig config) {
        this.config = config;

        spotifyApi = config.setupSecrets(new SpotifyApi.Builder())
                .setRedirectUri(config.getRedirectUri())
                .build();
    }

    public CompletableFuture<URI> requestAuthorisation() {
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

        return authCodeUriRequest.executeAsync();
    }
}
