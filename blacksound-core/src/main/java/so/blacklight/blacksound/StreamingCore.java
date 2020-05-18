package so.blacklight.blacksound;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import org.apache.hc.core5.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.session.SessionId;
import so.blacklight.blacksound.session.SessionStore;
import so.blacklight.blacksound.session.impl.FileSessionStore;
import so.blacklight.blacksound.spotify.SpotifyConfig;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StreamingCore {

    private final SpotifyApi spotifyApi;

    private final SessionStore<Subscriber> sessionStore = new FileSessionStore();
    private final Logger log = LogManager.getLogger(getClass());

    public StreamingCore(final SpotifyConfig config) {
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

    public void subscribe(final Subscriber subscriber) {
        final var sessionId = sessionStore.add(subscriber);
    }

    public void play(final String trackUri) {
        sessionStore.forEach(subscriber -> {
            final var playRequest = subscriber.getApi()
                    .startResumeUsersPlayback()
                    .context_uri(trackUri)
                    .build();

            try {
                final String result = playRequest.execute();

                System.out.println("Result: " + result);
            } catch (ParseException | IOException | SpotifyWebApiException e) {
                log.error("Error while playing song", e);
            }
        });
    }
}
