package so.blacklight.blacksound.web.handler;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.crypto.Crypto;
import so.blacklight.blacksound.subscriber.Subscriber;

public class ResumeHandler extends AuthenticatedHandler {

    public ResumeHandler(final StreamingCore core, final Vertx vertx, final Crypto crypto) {
        super(core, vertx, crypto);
    }

    @Override
    public void handle(final RoutingContext routingContext, final Subscriber subscriber) {
        log.info("Resume request received");

        subscriber.enableStreaming();
        subscriber.playSong(core.getChannel().currentTrack());
        subscriber.seekTrack(core.getChannel().getPlaybackPosition());

        final var response = routingContext.response();

        response.putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        response.end(asJson(new ResumeResponse("ok", subscriber.isStreamingEnabled())));
    }

    private static class ResumeResponse {

        private final String status;
        private final boolean streamingEnabled;

        public ResumeResponse(final String status, final boolean streamingEnabled) {
            this.status = status;
            this.streamingEnabled = streamingEnabled;
        }
    }
}
