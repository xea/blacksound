package so.blacklight.blacksound.web.handler;

import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;

public class PlayHandler implements VertxHandler {

    private final StreamingCore core;

    public PlayHandler(final StreamingCore core) {
        this.core = core;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        final var response = routingContext.response();

        response.putHeader("Content-type", "application/json");

        core.play();

        response.end(asJson(new StatusHandler.StatusResponse(core)));
    }
}
