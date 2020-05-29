package so.blacklight.blacksound.subscriber;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.Track;
import io.vavr.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.id.Identifiable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * An instance of a subscriber represents a user who had authenticated with spotify
 */
public class Subscriber implements Identifiable<SubscriberId> {

    public static final boolean ENABLED = true;
    public static final boolean DISABLED = false;

    private final SubscriberId id;
    private final SpotifyApi api;
    private boolean streamingEnabled;
    Instant expires;

    private final Logger log = LogManager.getLogger(getClass());

    public Subscriber(final SubscriberId id, final SpotifyApi api, final Instant expires, final boolean streamingEnabled) {
        this.id = id;
        this.api = api;
        this.expires = expires;
        this.streamingEnabled = streamingEnabled;
    }

    @Override
    public SubscriberId getId() {
        return id;
    }

    public SpotifyApi getApi() {
        return api;
    }

    public boolean refreshToken() {
        final boolean refreshed;

        if (needRefresh()) {
            final var refreshRequest = api.authorizationCodeRefresh().build();

            Try.of(refreshRequest::execute).toValidation(throwable -> {
                log.error("Failed to refresh token for user {} because of API error", id, throwable);

                return false;
            }).map(credentials -> {
                api.setAccessToken(credentials.getAccessToken());
                if (Objects.nonNull(credentials.getRefreshToken())) {
                    api.setRefreshToken(credentials.getRefreshToken());
                }
                expires = Instant.now().plus(credentials.getExpiresIn(), ChronoUnit.SECONDS);
                log.info("Refreshing token for user {} was successful", id);

                return true;
            });

            refreshed = true;
        } else {
            log.info("Skipping refresh for subscriber {}", id);

            refreshed = false;
        }

        return refreshed;
    }

    public boolean needRefresh() {
        return Instant.now().isAfter(expires);
    }

    public SubscriberHandle createHandle() {
        return new SubscriberHandle(id.toString(), api.getAccessToken(), api.getRefreshToken(), expires.toEpochMilli(), streamingEnabled);
    }

    public boolean isStreamingEnabled() {
        return streamingEnabled;
    }

    public String getCurrentTrack() {
        return Try.of(() -> api.getUsersCurrentlyPlayingTrack().build().execute())
                .map(currentlyPlaying -> {
                    final var item = currentlyPlaying.getItem();

                    if (item instanceof Track) {
                        final var track = (Track) item;

                        return track.getName() + " " + track.getUri();
                    }

                    return "Meh";
                })
                .getOrElse("None");
    }
}
