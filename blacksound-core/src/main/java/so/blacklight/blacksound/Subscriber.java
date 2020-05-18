package so.blacklight.blacksound;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import so.blacklight.blacksound.session.Session;
import so.blacklight.blacksound.session.SessionId;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Subscriber implements Session {

    private final SessionId id;
    private final SpotifyApi api;
    private Instant expires;

    public Subscriber(final String accessToken, final String refreshToken, final Instant expires) {
        this.id = new SessionId();
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

    @Override
    public SessionId getId() {
        return id;
    }

    public SpotifyApi getApi() {
        return api;
    }

    public void refreshToken() {
        final var refreshRequest = api.authorizationCodeRefresh().build();

        refreshRequest.executeAsync().thenAcceptAsync(credentials -> {
            api.setAccessToken(credentials.getAccessToken());
            api.setRefreshToken(credentials.getRefreshToken());
            expires = Instant.now().plus(credentials.getExpiresIn(), ChronoUnit.SECONDS);
        });
    }

    public boolean needRefresh() {
        return Instant.now().isAfter(expires);
    }
}
