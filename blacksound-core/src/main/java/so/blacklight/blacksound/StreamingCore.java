package so.blacklight.blacksound;

import com.google.gson.JsonParser;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import io.vavr.control.Option;
import org.apache.hc.core5.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.spotify.SpotifyConfig;
import so.blacklight.blacksound.subscriber.FileSubscriberStore;
import so.blacklight.blacksound.subscriber.Subscriber;
import so.blacklight.blacksound.subscriber.SubscriberId;
import so.blacklight.blacksound.subscriber.SubscriberStore;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class StreamingCore {

    private final SpotifyApi spotifyApi;
    private final SubscriberStore subscriberStore;
    private final List<Subscriber> subscribers;

    private final Logger log = LogManager.getLogger(getClass());
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(4);

    public StreamingCore(final SpotifyConfig config) {
        spotifyApi = config.setupSecrets(new SpotifyApi.Builder())
                .setRedirectUri(config.getRedirectUri())
                .build();

        subscribers = new CopyOnWriteArrayList<>();
        subscriberStore = new FileSubscriberStore();

        subscribers.addAll(subscriberStore.loadEntries(subscriberHandle -> {
            final var subscriberId = new SubscriberId(subscriberHandle.getId());
            final var subscriberApi = new SpotifyApi.Builder()
                    .setClientId(spotifyApi.getClientId())
                    .setClientSecret(spotifyApi.getClientSecret())
                    .setAccessToken(subscriberHandle.getAccessToken())
                    .setRefreshToken(subscriberHandle.getRefreshToken())
                    .build();

            final var subscriberExpires = Instant.ofEpochMilli(subscriberHandle.getExpires());

            return new Subscriber(subscriberId, subscriberApi, subscriberExpires, subscriberHandle.isEnabled());
        }));

        scheduler.scheduleAtFixedRate(this::refreshSubscribers, 0, 10, TimeUnit.MINUTES);
    }

    public URI getAuthorizationURI() {
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

    public SubscriberId register(final AuthorizationCodeCredentials credentials) {
        final var id = new SubscriberId();
        final var api = new SpotifyApi.Builder()
                .setClientId(spotifyApi.getClientId())
                .setClientSecret(spotifyApi.getClientSecret())
                .setAccessToken(credentials.getAccessToken())
                .setRefreshToken(credentials.getRefreshToken())
                .build();

        final var expires = Instant.now().plus(credentials.getExpiresIn(), ChronoUnit.SECONDS);

        subscribers.add(new Subscriber(id, api, expires, Subscriber.ENABLED));

        updateSubscribers();

        return id;
    }

    private void updateSubscribers() {
        final var handles = subscribers.stream()
                .map(Subscriber::createHandle)
                .collect(Collectors.toSet());

        synchronized (subscriberStore) {
            subscriberStore.saveEntries(handles);
        }
    }

    public void play(final String trackUri) {
        subscribers.forEach(subscriber -> {
            final var playRequest = subscriber.getApi()
                    .startResumeUsersPlayback()
                    .uris(JsonParser.parseString("[ \"" + trackUri + "\" ]").getAsJsonArray())
                    .build();

            try {
                final String result = playRequest.execute();

                System.out.println("Result: " + result);
            } catch (ParseException | IOException | SpotifyWebApiException e) {
                log.error("Error while playing song", e);
            }
        });
    }

    public void pause() {
        subscribers.forEach(subscriber -> {
            final var pauseRequest = subscriber.getApi().pauseUsersPlayback().build();

            try {
                final var result = pauseRequest.execute();

                log.debug("Pause request result: {}", result);
            } catch (ParseException | SpotifyWebApiException | IOException e) {
                log.error("Error during pause", e);
            }
        });
    }

    private void refreshSubscribers() {
        log.debug("Refreshing access tokens");

        final var refreshed = subscribers.stream()
                .map(Subscriber::refreshToken)
                .filter(e -> e)
                .count();

        updateSubscribers();

        log.info("Refreshed {} access tokens", refreshed);
    }

    public Optional<Subscriber> findSubscriber(final SubscriberId subscriberId) {
        return subscribers.stream()
                .filter(subscriber -> subscriber.getId().equals(subscriberId))
                .findAny();
    }
}
