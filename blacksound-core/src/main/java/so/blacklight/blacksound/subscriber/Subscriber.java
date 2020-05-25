package so.blacklight.blacksound.subscriber;

import com.wrapper.spotify.SpotifyApi;
import io.vavr.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.id.Identifiable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Subscriber implements Identifiable<SubscriberId> {

    public static final boolean ENABLED = true;
    public static final boolean DISABLED = false;

    private final SubscriberId id;
    private final SpotifyApi api;
    private boolean enabled;
    Instant expires;

    private final Logger log = LogManager.getLogger(getClass());

    public Subscriber(final SubscriberId id, final SpotifyApi api, final Instant expires, final boolean enabled) {
        this.id = id;
        this.api = api;
        this.expires = expires;
        this.enabled = enabled;
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
                log.error("Failed to refresh token for user {} because of API error", id.toString(), throwable);

                return false;
            }).map(credentials -> {
                api.setAccessToken(credentials.getAccessToken());
                api.setRefreshToken(credentials.getRefreshToken());
                expires = Instant.now().plus(credentials.getExpiresIn(), ChronoUnit.SECONDS);
                log.info("Refreshing token for user {} was successful", id.toString());

                return true;
            });

            refreshed = true;
        } else {
            log.info("Skipping refresh for subscriber {}", id.toString());

            refreshed = false;
        }

        return refreshed;
    }

    public boolean needRefresh() {
        return Instant.now().isAfter(expires);
    }

    public SubscriberHandle createHandle() {
        return new SubscriberHandle(id.toString(), api.getAccessToken(), api.getRefreshToken(), expires.toEpochMilli(), enabled);
    }

    public Subscriber enable() {
        this.enabled = ENABLED;

        return this;
    }

    public Subscriber disable() {
        this.enabled = DISABLED;

        return this;
    }

}
