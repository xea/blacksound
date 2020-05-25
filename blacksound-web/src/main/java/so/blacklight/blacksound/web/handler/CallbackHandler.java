package so.blacklight.blacksound.web.handler;

import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.CookieSameSite;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.crypto.Crypto;

public class CallbackHandler implements VertxHandler {

    private final Vertx vertx;
    private final StreamingCore core;
    private final Crypto crypto;

    private final Logger log = LogManager.getLogger(getClass());

    public CallbackHandler(final StreamingCore core, final Vertx vertx, final Crypto crypto) {
        this.core = core;
        this.vertx = vertx;
        this.crypto = crypto;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        final var request = routingContext.request();
        final var response = routingContext.response();

        final var code = request.getParam("code");

        core.requestAuthorisation(code).thenAcceptAsync(credentials -> {
            final var id = core.register(credentials);

            // Store the subscriber id on in an encrypted cookie on the client side
            final var encryptedId = crypto.encryptAndEncode64(id.toString().getBytes());

            final var subscriberCookie = Cookie.cookie(SESSION_KEY, encryptedId);
            subscriberCookie.setHttpOnly(true);
            subscriberCookie.setSameSite(CookieSameSite.STRICT);

            routingContext.addCookie(subscriberCookie);

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
