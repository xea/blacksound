package so.blacklight.blacksound.web.handler;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.Subscriber;

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
            final var subscriber = new Subscriber(credentials);

            final var id = core.register(subscriber);

            routingContext.session().put("subscription-id", id.value().toString());

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
