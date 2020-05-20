package so.blacklight.blacksound;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import org.apache.hc.core5.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.spotify.SpotifyConfig;
import so.blacklight.blacksound.subscriber.FileSubscriberStore;
import so.blacklight.blacksound.subscriber.Subscriber;
import so.blacklight.blacksound.subscriber.SubscriberStore;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class StreamingCore {

    private final SpotifyApi spotifyApi;

    private final Logger log = LogManager.getLogger(getClass());
    private final SubscriberStore subscribers = new FileSubscriberStore();
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(4);

    public StreamingCore(final SpotifyConfig config) {
        spotifyApi = config.setupSecrets(new SpotifyApi.Builder())
                .setRedirectUri(config.getRedirectUri())
                .build();

        scheduler.scheduleAtFixedRate(this::refreshSubscribers, 1, 30, TimeUnit.MINUTES);
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

    public void register(final Subscriber subscriber) {
        subscribers.register(subscriber);
    }

    public void play(final String trackUri) {
        subscribers.forEach(subscriber -> {
            final var playRequest = subscriber.getApi()
                    .startResumeUsersPlayback()
                    .context_uri(trackUri)
                    .build();

            try {
                final String result = playRequest.execute();

                System.out.println("Result: " + result);

                return true;
            } catch (ParseException | IOException | SpotifyWebApiException e) {
                log.error("Error while playing song", e);
            }

            return false;
        });
    }

    private void refreshSubscribers() {
        log.debug("Refreshing access tokens");

        final var refreshed = subscribers.forEach(Subscriber::refreshToken);
        subscribers.save();

        if (refreshed > 0) {
            log.info("Refreshed {} access tokens", refreshed);
        }
    }
}
