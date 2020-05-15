package so.blacklight.blacksound.web.handler;

import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;

public class StatusHandler implements VertxHandler {

    private final StreamingCore core;

    public StatusHandler(StreamingCore core) {
        this.core = core;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        final var response = routingContext.response();

        final var status = new StatusResponse(core);

        response.end(asJson(status));
    }

    static class StatusResponse {

        public final String status;

        public StatusResponse(final StreamingCore core) {
            status = "ok";
        }
    }
}
