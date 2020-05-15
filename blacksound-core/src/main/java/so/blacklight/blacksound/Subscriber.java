package so.blacklight.blacksound;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class Subscriber {

    private final UUID id;
    private final SpotifyApi api;
    private final Instant expires;

    public Subscriber(final String accessToken, final String refreshToken, final Instant expires) {
        this.id = UUID.randomUUID();
        this.expires = expires;

        api = new SpotifyApi.Builder()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .build();
    }

    public Subscriber(AuthorizationCodeCredentials credentials) {
        this(
                credentials.getAccessToken(),
                credentials.getRefreshToken(),
                Instant.now().plus(credentials.getExpiresIn(), ChronoUnit.SECONDS));
    }

    public UUID getId() {
        return id;
    }

    public SpotifyApi getApi() {
        return api;
    }

    public boolean needRefresh() {
        return Instant.now().isAfter(expires);
    }
}
