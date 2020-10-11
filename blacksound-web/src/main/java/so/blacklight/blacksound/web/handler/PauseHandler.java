package so.blacklight.blacksound.web.handler;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.hc.core5.http.ContentType;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.crypto.Crypto;
import so.blacklight.blacksound.subscriber.Subscriber;

public class PauseHandler extends AuthenticatedHandler {

    public PauseHandler(StreamingCore core, Vertx vertx, Crypto crypto) {
        super(core, vertx, crypto);
    }

    @Override
    public void handle(RoutingContext routingContext, Subscriber subscriber) {
        subscriber.disableStreaming();
        subscriber.pause();

        final var response = routingContext.response();
        response.putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        response.end(asJson(new ResumeResponse("ok", subscriber.isStreamingEnabled())));
    }

    private static class ResumeResponse {

        public final String status;
        public final boolean streamingEnabled;

        public ResumeResponse(final String status, final boolean streamingEnabled) {
            this.status = status;
            this.streamingEnabled = streamingEnabled;
        }
    }
}
