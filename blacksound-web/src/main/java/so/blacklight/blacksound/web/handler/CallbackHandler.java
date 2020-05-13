package so.blacklight.blacksound.web.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class CallbackHandler implements Handler<RoutingContext> {
    public CallbackHandler() {

    }

    @Override
    public void handle(RoutingContext routingContext) {
        final var response = routingContext.response();
        response.putHeader("Content-Type", "text/plain");
        response.end("Callback received");
    }
}
