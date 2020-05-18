package so.blacklight.blacksound.web.handler;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.Subscriber;

public class SubscribeHandler implements Handler<RoutingContext> {

    private final Vertx vertx;
    private final StreamingCore core;

    private final Logger log = LogManager.getLogger(getClass());

    public SubscribeHandler(final StreamingCore core, final Vertx vertx) {
        this.core = core;
        this.vertx = vertx;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        final var request = routingContext.request();
        final var response = routingContext.response();

        /*
        final var code = request.getParam("code");

        core.requestAuthorisation(code).thenAcceptAsync(credentials -> {
            response.putHeader("Content-Type", "text/plain");

            final var subscriber = new Subscriber(credentials);

            core.subscribe(subscriber);

            log.info("Registered new subscriber with ID {}", subscriber.getId());

            response.end("Accepted credentials, will expire in " + credentials.getExpiresIn());
        }, vertx.nettyEventLoopGroup());
         */
    }
}
