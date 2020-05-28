package so.blacklight.blacksound.web.handler;

import com.google.gson.Gson;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;

public class QueueHandler implements VertxHandler {

    private final StreamingCore core;
    private final Vertx vertx;

    public QueueHandler(final StreamingCore core, final Vertx vertx) {
        this.core = core;
        this.vertx = vertx;
    }

    @Override
    public void handle(final RoutingContext routingContext) {
        final var body = routingContext.getBodyAsString();
        final var queueRequest = new Gson().fromJson(body, QueueRequest.class);
        final var response = routingContext.response();

        core.queue(queueRequest.trackUri);

        response.end(asJson(new QueueResponse("ok")));
    }

    private static class QueueRequest {

        private String trackUri;

    }

    private static class QueueResponse {

        private final String status;

        public QueueResponse(final String status) {
            this.status = status;
        }
    }
}
