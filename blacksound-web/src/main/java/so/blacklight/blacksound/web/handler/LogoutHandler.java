package so.blacklight.blacksound.web.handler;

import io.vavr.control.Option;
import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.crypto.Crypto;
import so.blacklight.blacksound.subscriber.SubscriberId;

import java.util.concurrent.CompletableFuture;

public class LogoutHandler implements VertxHandler {

    private final StreamingCore core;
    private final Crypto crypto;
    private final Vertx vertx;
    private final Logger log = LogManager.getLogger(getClass());

    public LogoutHandler(final StreamingCore core, final Vertx vertx, final Crypto crypto) {
        this.core = core;
        this.crypto = crypto;
        this.vertx = vertx;
    }

    @Override
    public void handle(final RoutingContext routingContext) {
        final var response = routingContext.response();

        CompletableFuture.runAsync(() -> {
            Option.of(routingContext.getCookie(SESSION_KEY))
                    .peek(cookie -> log.debug("Found session cookie"))
                    .map(Cookie::getValue)
                    .toValidation(() -> "Subscriber cookie was not found")
                    .flatMap(crypto::decode64AndDecrypt)
                    .map(String::new)
                    .peek(sessionId -> log.debug("Session has id {}", sessionId))
                    .map(SubscriberId::new)
                    .map(core::unregister)
                    .peek(success -> log.info("User unregistered: {}", success));

            response.setStatusCode(HttpStatus.SC_MOVED_TEMPORARILY);
            response.putHeader(HttpHeaders.LOCATION, "/");
            response.end("{ \"status\": \"ok\" }");
        }, vertx.nettyEventLoopGroup());
    }
}
