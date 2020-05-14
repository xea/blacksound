package so.blacklight.blacksound.web.handler;

import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class AuthorisationHandler implements VertxHandler {

    private StreamingCore core;

    @Override
    public void handle(RoutingContext routingContext) {
        //final CompletableFuture<URI> uriCompletableFuture = core.requestAuthorisationURI();
    }
}
