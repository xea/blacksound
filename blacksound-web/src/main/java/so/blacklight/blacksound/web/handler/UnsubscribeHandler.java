package so.blacklight.blacksound.web.handler;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;

public class UnsubscribeHandler implements VertxHandler {

    private final StreamingCore core;
    private final Vertx vertx;

    public UnsubscribeHandler(final StreamingCore core, final Vertx vertx) {
        this.core = core;
        this.vertx = vertx;
    }

    @Override
    public void handle(RoutingContext routingContext) {
    }
}
