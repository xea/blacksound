package so.blacklight.blacksound.subscriber;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import so.blacklight.blacksound.id.Identifiable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class Subscriber implements Identifiable<SubscriberId> {

    private final SubscriberId id;
    private final SpotifyApi api;
    Instant expires;

    public Subscriber(final SubscriberId id, final String accessToken, final String refreshToken, final Instant expires) {
        this.id = Optional.ofNullable(id).orElse(new SubscriberId());
        this.expires = expires;

        api = new SpotifyApi.Builder()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .build();
    }

    public Subscriber(AuthorizationCodeCredentials credentials) {
        this(new SubscriberId(), credentials);
    }

    public Subscriber(final SubscriberId id, final AuthorizationCodeCredentials credentials) {
        this(
                id,
                credentials.getAccessToken(),
                credentials.getRefreshToken(),
                Instant.now().plus(credentials.getExpiresIn(), ChronoUnit.SECONDS));
    }

    @Override
    public SubscriberId getId() {
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
