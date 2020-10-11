package so.blacklight.blacksound.subscriber;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.miscellaneous.Device;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.User;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import org.apache.hc.core5.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.id.Identifiable;
import so.blacklight.blacksound.stream.Song;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * An instance of a subscriber represents a user who had authenticated with spotify
 */
public class Subscriber implements Identifiable<SubscriberId> {

    public static final boolean ENABLED = true;
    public static final boolean DISABLED = false;

    private final SubscriberId id;
    private final SpotifyApi api;
    private boolean streamingEnabled;
    private Instant expires;

    private String activeDeviceId;

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

    public String getProfileName() {
        return Try.of(() -> api.getCurrentUsersProfile().build().execute())
                .map(User::getDisplayName)
                .getOrElse("Unidentified user :(");
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

    public Validation<String, Song> lookupSong(final String trackUri) {
        if (Objects.isNull(trackUri)) {
            return Validation.invalid("Null track id");
        } else {
            final var sanitizedTrackUri = trackUri.trim();

            if (sanitizedTrackUri.startsWith("spotify:track:")) {
                return Try.of(() -> getApi()
                            .getTrack(sanitizedTrackUri.substring(14))
                            .build()
                            .execute())
                        .map(Song::new)
                        .toValidation(Throwable::getMessage);
            } else {
                return Validation.invalid("Not a track URI");
            }
        }

    }

    public void enableStreaming() {
        streamingEnabled = ENABLED;
    }

    public void playSong(final Song song) {
        playSong(song.getUri());
    }

    public void playSong(final String trackUri) {
        final var playRequest = getApi()
                .startResumeUsersPlayback()
                .uris(JsonParser.parseString("[ \"" + trackUri + "\" ]").getAsJsonArray())
                .build();

        try {
            playRequest.execute();
        } catch (ParseException | IOException | SpotifyWebApiException e) {
            log.error("Error while playing song", e);
        }
    }

    public void seekTrack(int playbackPosition) {
        final var seekRequest = getApi()
                .seekToPositionInCurrentlyPlayingTrack(playbackPosition * 1000)
                .build();

        try {
            seekRequest.execute();
        } catch (ParseException | SpotifyWebApiException | IOException e) {
            log.error("Error while seeking into the song");
        }
    }

    public List<Device> getDevices() {
        final var devicesRequest = getApi().getUsersAvailableDevices().build();

        try {
            final Device[] devices = devicesRequest.execute();

            Arrays.sort(devices, Comparator.comparing(Device::getIs_active).reversed());

            return Arrays.asList(devices);
        } catch (ParseException | IOException | SpotifyWebApiException e) {
            log.error("Error finding devices", e);
        }

        return Collections.emptyList();
    }

    public void setActiveDevice(final String deviceId) {
        JsonArray deviceIds = new JsonArray();
        deviceIds.add(deviceId);

        final var setDeviceRequest = getApi().transferUsersPlayback(deviceIds).build();

        activeDeviceId = deviceId;

        try {
            setDeviceRequest.execute();
        } catch (ParseException | SpotifyWebApiException | IOException e) {
            log.error("Error setting active device {}", e.getMessage());
        }
    }
}
