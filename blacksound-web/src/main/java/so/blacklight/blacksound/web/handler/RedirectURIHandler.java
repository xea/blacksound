package so.blacklight.blacksound.web.handler;

import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;

public class RedirectURIHandler implements VertxHandler {

    private final StreamingCore core;

    public RedirectURIHandler(final StreamingCore core) {
        this.core = core;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        final var response = routingContext.response();

        final var uri = core.requestAuthorisationURI();

        response.putHeader("Content-type", "application/json");

        response.end(asJson(new RedirectURIResponse(uri.toString())));
    }

    private static class RedirectURIResponse {

        public final String redirectUri;

        public RedirectURIResponse(final String redirectUri) {
            this.redirectUri = redirectUri;
        }
    }
}
