package so.blacklight.blacksound.web.handler;

import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;

public class AuthorisationHandler implements VertxHandler {

    private StreamingCore core;

    @Override
    public void handle(RoutingContext routingContext) {
        final var uri = core.requestAuthorisationURI();


    }
}
