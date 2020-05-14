package so.blacklight.blacksound.web.handler;

import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.web.Server;

public class ShutDownHandler implements VertxHandler {

    private final Logger log = LogManager.getLogger(getClass());

    private final Server server;

    public ShutDownHandler(final Server server) {
        this.server = server;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        log.info("Received shutdown request via web client");

        final var response = routingContext.response();

        response.putHeader("Content-Type", "application/json");
        response.end("{ \"status\": \"ok\" }");

        server.shutDown();
    }
}
