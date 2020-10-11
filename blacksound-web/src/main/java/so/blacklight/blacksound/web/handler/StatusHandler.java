package so.blacklight.blacksound.web.handler;

import io.vavr.control.Option;
import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.crypto.Crypto;
import so.blacklight.blacksound.stream.Song;
import so.blacklight.blacksound.subscriber.Subscriber;
import so.blacklight.blacksound.subscriber.SubscriberId;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static so.blacklight.blacksound.web.handler.AuthenticatedHandler.SESSION_KEY;

public class StatusHandler implements VertxHandler {

    private final StreamingCore core;
    private final Vertx vertx;
    private final Crypto crypto;
    private final Logger log = LogManager.getLogger(getClass());

    public StatusHandler(final StreamingCore core, final Vertx vertx, final Crypto crypto) {
        this.core = core;
        this.vertx = vertx;
        this.crypto = crypto;
    }

    @Override
    public void handle(final RoutingContext routingContext) {
        CompletableFuture.runAsync(() -> {
            final var response = Option.of(routingContext.getCookie(SESSION_KEY))
                    .peek(cookie -> log.debug("Found session cookie"))
                    .map(Cookie::getValue)
                    .toValidation(() -> "Subscriber cookie was not found")
                    .flatMap(crypto::decode64AndDecrypt)
                    .map(String::new)
                    .peek(sessionId -> log.debug("Session has id {}", sessionId))
                    .map(SubscriberId::new)
                    .flatMap(id -> core.findSubscriber(id).toValidation(() -> "No such subscriber"))
                    .map(this::authenticatedResponse)
                    .getOrElseGet(this::unauthenticatedResponse);

            final var httpResponse = routingContext.response();

            httpResponse.putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            httpResponse.end(asJson(response));
        }, vertx.nettyEventLoopGroup());
    }

    private StatusResponse authenticatedResponse(final Subscriber subscriber) {
        final var name = subscriber.getProfileName();
        final var currentTrack = subscriber.getCurrentTrack();
        final var playlist = core.getChannel().getPlaylist();

        return new AuthenticatedStatusResponse(name, subscriber.isStreamingEnabled(), currentTrack, playlist);
    }

    private StatusResponse unauthenticatedResponse(final String error) {
        return new UnauthenticatedStatusResponse(core.getAuthorizationURI().toASCIIString());
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

    static class UnauthenticatedStatusResponse extends StatusResponse {

        public final String redirectUri;

        public UnauthenticatedStatusResponse(final String redirectUri) {
            super(false);

            this.redirectUri = redirectUri;
        }
    }

    static class AuthenticatedStatusResponse extends StatusResponse {

        public final String name;
        public final boolean streamingEnabled;
        public final String currentTrack;
        public final List<String> playlist;

        public AuthenticatedStatusResponse(final String name, final boolean streamingEnabled, final String currentTrack, final List<Song> playlist) {
            super(true);

            this.name = name;
            this.streamingEnabled = streamingEnabled;
            this.currentTrack = currentTrack;
            this.playlist = playlist.stream().map(Song::getFullTitle).collect(Collectors.toList());
        }

    }
}
