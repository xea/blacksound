package so.blacklight.blacksound.web.handler;

import com.wrapper.spotify.model_objects.miscellaneous.Device;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.hc.core5.http.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.crypto.Crypto;
import so.blacklight.blacksound.stream.Song;
import so.blacklight.blacksound.subscriber.Subscriber;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class StatusHandler extends AuthenticatedHandler {

    private final Logger log = LogManager.getLogger(getClass());

    public StatusHandler(final StreamingCore core, final Vertx vertx, final Crypto crypto) {
        super(core, vertx, crypto);
    }

    @Override
    public void handle(RoutingContext routingContext, Subscriber subscriber) {
        final var response = authenticatedResponse(subscriber);
        final var httpResponse = routingContext.response();

        httpResponse.putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        httpResponse.end(asJson(response));
    }

    private StatusResponse authenticatedResponse(final Subscriber subscriber) {
        final var name = subscriber.getProfileName();
        final var currentTrack = Optional.ofNullable(core.getChannel().currentTrack())
                .orElse(new Song(null, "None", "None", 0));

        final var currentTrackTitle = currentTrack.getFullTitle();
        final var currentTrackLength = currentTrack.getLength(TimeUnit.SECONDS);
        final var playbackPosition = core.getChannel().getPlaybackPosition();
        final var playlist = core.getChannel().getPlaylist();
        final var devices = subscriber.getDevices();
        final var streamingEnabled = subscriber.isStreamingEnabled();
        final var activeUsers = core.getActiveSubscribers();

        subscriber.updateSeen();

        return new AuthenticatedStatusResponse(name, streamingEnabled, currentTrackTitle, currentTrackLength, playbackPosition, playlist, devices, activeUsers);
    }

    /**
     * Base class for supplying mandatory response parameters
     */
    abstract static class StatusResponse {

        public final boolean hasSession;
        public final String status;

        public StatusResponse(final boolean hasSession) {
            status = "ok";
            this.hasSession = hasSession;
        }
    }

    static class AuthenticatedStatusResponse extends StatusResponse {

        public final String name;
        public final boolean streamingEnabled;
        public final int playbackPosition;
        public final String currentTrack;
        public final long currentTrackLength;
        public final List<String> playlist;
        public final List<Device> devices;
        public final List<String> activeUsers;

        public AuthenticatedStatusResponse(final String name, final boolean streamingEnabled, final String currentTrack, final long currentTrackLength, final int playbackPosition, final List<Song> playlist, final List<Device> devices, final List<String> activeUsers) {
            super(true);

            this.name = name;
            this.streamingEnabled = streamingEnabled;
            this.currentTrack = currentTrack;
            this.currentTrackLength = currentTrackLength;
            this.playbackPosition = playbackPosition;
            this.playlist = playlist.stream().map(Song::getPrettyTitle).collect(Collectors.toList());
            this.devices = devices;
            this.activeUsers = activeUsers;
        }

    }
}
