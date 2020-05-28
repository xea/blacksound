package so.blacklight.blacksound.web.handler;

import com.google.gson.Gson;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;

import java.util.concurrent.CompletableFuture;

public class PlayHandler implements VertxHandler {

    private final StreamingCore core;
    private final Vertx vertx;

    public PlayHandler(final StreamingCore core, final Vertx vertx) {
        this.core = core;
        this.vertx = vertx;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        final var body = routingContext.getBodyAsString();
        final var playRequest = new Gson().fromJson(body, PlayRequest.class);
        final var response = routingContext.response();

        response.putHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        CompletableFuture.runAsync(() -> {
            playRequest(playRequest.trackUri);
        }, vertx.nettyEventLoopGroup())
                .thenAccept(v -> response.end(asJson(new PlayResponse("ok"))));

    }

    private void playRequest(final String trackUri) {
        if (trackUri == null) {
            core.play();
        } else {
            core.playTrack(trackUri);
        }
    }

    private static class PlayRequest {

        private String trackUri;

    }

    private static class PlayResponse {

        public final String status;

        public PlayResponse(final String status) {
            this.status = status;
        }
    }
}
