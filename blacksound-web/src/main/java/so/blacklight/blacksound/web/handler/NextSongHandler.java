package so.blacklight.blacksound.web.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NextSongHandler implements Handler<RoutingContext> {

    private final Logger log = LogManager.getLogger(getClass());

    @Override
    public void handle(RoutingContext routingContext) {
        log.info("Received next-song request");
    }
}
