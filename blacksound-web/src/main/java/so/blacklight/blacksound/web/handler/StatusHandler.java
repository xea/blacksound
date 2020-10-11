package so.blacklight.blacksound.web.handler;

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
        final var currentTrack = subscriber.getCurrentTrack();
        final var playbackPosition = core.getChannel().getPlaybackPosition();
        final var playlist = core.getChannel().getPlaylist();

        return new AuthenticatedStatusResponse(name, subscriber.isStreamingEnabled(), currentTrack, playbackPosition, playlist);
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
        public final List<String> playlist;

        public AuthenticatedStatusResponse(final String name, final boolean streamingEnabled, final String currentTrack, final int playbackPosition, final List<Song> playlist) {
            super(true);

            this.name = name;
            this.streamingEnabled = streamingEnabled;
            this.currentTrack = currentTrack;
            this.playbackPosition = playbackPosition;
            this.playlist = playlist.stream().map(Song::getPrettyTitle).collect(Collectors.toList());
        }

    }
}
