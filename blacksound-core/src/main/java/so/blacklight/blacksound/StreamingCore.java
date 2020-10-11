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
import so.blacklight.blacksound.stream.Channel;
import so.blacklight.blacksound.stream.Song;
import so.blacklight.blacksound.subscriber.FileSubscriberStore;
import so.blacklight.blacksound.subscriber.Subscriber;
import so.blacklight.blacksound.subscriber.SubscriberId;
import so.blacklight.blacksound.subscriber.SubscriberStore;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class StreamingCore {

    private final SpotifyApi spotifyApi;
    private final SubscriberStore subscriberStore;
    private final List<Subscriber> subscribers;
    private final Channel channel;

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
        scheduler.scheduleAtFixedRate(this::updateSubscribers, 15, 30, TimeUnit.SECONDS);

        channel = new Channel(this::playTrack);
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

        subscribers.add(new Subscriber(id, api, expires, Subscriber.DISABLED));

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

    protected void playTrack(final Song song) {
        playTrack(song.getUri());
    }

    public void playTrack(final String trackUri) {
        subscribers.stream()
                .filter(Subscriber::isStreamingEnabled)
                .forEach(subscriber -> subscriber.playSong(trackUri));
    }

    public boolean queue(final Song song) {
        return channel.queueTrack(song);
    }

    public void play() {
        subscribers.stream().filter(Subscriber::isStreamingEnabled).forEach(subscriber -> {
            final var playRequest = subscriber.getApi().startResumeUsersPlayback().build();

            try {
                final var result = playRequest.execute();

            } catch (IOException | SpotifyWebApiException | ParseException e) {
                log.error("Error while resuming playback", e);
            }
        });
    }

    public void pause() {
        subscribers.stream().filter(s -> !s.isStreamingEnabled()).forEach(Subscriber::pause);
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

    public Option<Subscriber> findSubscriber(final SubscriberId subscriberId) {
        return Option.ofOptional(subscribers.stream()
                .filter(subscriber -> subscriber.getId().equals(subscriberId))
                .findAny());
    }

    public boolean unregister(final SubscriberId id) {
        final var removed = subscribers.removeIf(subscriber -> subscriber.equalsId(id));

        updateSubscribers();

        return removed;
    }

    public Channel getChannel() {
        return channel;
    }
}
