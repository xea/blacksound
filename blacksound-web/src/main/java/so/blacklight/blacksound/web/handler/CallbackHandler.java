package so.blacklight.blacksound.web.handler;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;

public class CallbackHandler implements Handler<RoutingContext> {

    private final Vertx vertx;
    private final StreamingCore core;

    public CallbackHandler(final StreamingCore core, final Vertx vertx) {
        this.core = core;
        this.vertx = vertx;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        final var request = routingContext.request();
        final var response = routingContext.response();

        final var code = request.getParam("code");

        core.requestAuthorisation(code).thenAcceptAsync(credentials -> {
            response.putHeader("Content-Type", "text/plain");

            response.end("Accepted credentials, will expire in " + credentials.getExpiresIn());
        }, vertx.nettyEventLoopGroup());
    }
}
