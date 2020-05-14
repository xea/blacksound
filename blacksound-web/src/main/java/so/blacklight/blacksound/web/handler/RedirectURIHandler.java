package so.blacklight.blacksound.web.handler;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;

public class RedirectURIHandler implements VertxHandler {

    private final StreamingCore core;
    private final Vertx vertx;

    public RedirectURIHandler(final StreamingCore core, Vertx vertx) {
        this.core = core;
        this.vertx = vertx;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        final var response = routingContext.response();

        core.requestAuthorisation().thenAcceptAsync(uri -> {
            response.putHeader("Content-type", "application/json");

            response.end(asJson(new RedirectURIResponse(uri.toString())));
        }, vertx.nettyEventLoopGroup());
    }

    private static class RedirectURIResponse {

        public final String redirectUri;

        public RedirectURIResponse(final String redirectUri) {
            this.redirectUri = redirectUri;
        }
    }
}
