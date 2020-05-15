package so.blacklight.blacksound.web.handler;

import io.vertx.ext.web.RoutingContext;

public class StatusHandler implements VertxHandler {

    public StatusHandler() {
        // Yet to be populated
    }

    @Override
    public void handle(RoutingContext routingContext) {
        final var response = routingContext.response();

        final var status = new StatusResponse();

        response.end(asJson(status));
    }

    static class StatusResponse {

        public final String status;

        public StatusResponse() {
            status = "ok";
        }
    }
}
