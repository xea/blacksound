package so.blacklight.blacksound.web.handler;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.hc.core5.http.ContentType;
import so.blacklight.blacksound.StreamingCore;

import java.util.concurrent.CompletableFuture;

public class PauseHandler implements VertxHandler {

    private final StreamingCore core;
    private final Vertx vertx;

    public PauseHandler(final StreamingCore core, final Vertx vertx) {
        this.core = core;
        this.vertx = vertx;
    }

    @Override
    public void handle(final RoutingContext routingContext) {
        CompletableFuture.runAsync(() -> {
            core.pause();

            final var response = routingContext.response();
            response.putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            response.end(asJson(new ResumeResponse("ok", false)));
        }, vertx.nettyEventLoopGroup());

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
