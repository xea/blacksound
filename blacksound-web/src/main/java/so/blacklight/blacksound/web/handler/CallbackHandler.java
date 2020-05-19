package so.blacklight.blacksound.web.handler;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.subscriber.Subscriber;
import so.blacklight.blacksound.subscriber.SubscriberId;

public class CallbackHandler implements VertxHandler {

    private final Vertx vertx;
    private final StreamingCore core;

    private final Logger log = LogManager.getLogger(getClass());

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
            final var id = new SubscriberId();
            final var subscriber = new Subscriber(id, credentials);

            // Using the session store to persist the subscriber id on the server side will suffice for now, but later
            // we might want to push it to the client side
            routingContext.session().put("subscriber-id", id.value().toString());

            core.register(subscriber);

            log.info("Registered new subscriber with ID {}", id);

            response.end(asJson(new RegistrationResponse("ok")));
        }, vertx.nettyEventLoopGroup());
    }

    private static class RegistrationResponse {
        public final String status;

        public RegistrationResponse(final String status) {
            this.status = status;
        }
    }
}
