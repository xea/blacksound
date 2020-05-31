package so.blacklight.blacksound.web.handler;

import io.vavr.control.Option;
import io.vavr.control.Validation;
import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.crypto.Crypto;
import so.blacklight.blacksound.subscriber.Subscriber;
import so.blacklight.blacksound.subscriber.SubscriberId;

import java.util.concurrent.CompletableFuture;

public abstract class AuthenticatedHandler extends AbstractHandler {

    public static final String SESSION_KEY = "subscriber-id";

    private final StreamingCore core;
    private final Vertx vertx;
    private final Crypto crypto;

    public AuthenticatedHandler(final StreamingCore core, final Vertx vertx, final Crypto crypto) {
        this.core = core;
        this.vertx = vertx;
        this.crypto = crypto;
    }

    @Override
    public void handle(final RoutingContext routingContext) {
        CompletableFuture.supplyAsync(() -> resolveSubscriberId(routingContext), vertx.nettyEventLoopGroup())
                .thenApplyAsync(this::resolveSubscriber, vertx.nettyEventLoopGroup())
                .thenAcceptAsync(result ->
                        result.peek(subscriber -> handle(routingContext, subscriber)),
                        vertx.nettyEventLoopGroup());
        /*
        Option.of(routingContext.getCookie(SESSION_KEY))
                .peek(cookie -> log.debug("Found session cookie"))
                .map(Cookie::getValue)
                .toValidation(() -> "Subscriber cookie was not found")
                .flatMap(crypto::decode64AndDecrypt)
                .map(String::new)
                .peek(sessionId -> log.debug("Session has id {}", sessionId))
                .map(SubscriberId::new)
                .flatMap(id -> core.findSubscriber(id).toValidation(() -> "No such subscriber"))
                .peek(subscriber -> handle(routingContext, subscriber));
         */
    }


    private Validation<String, SubscriberId> resolveSubscriberId(final RoutingContext routingContext) {
        return Option.of(routingContext.getCookie(SESSION_KEY))
                .peek(cookie -> log.debug("Found session cookie"))
                .map(Cookie::getValue)
                .toValidation(() -> "Subscriber cookie was not found")
                .flatMap(crypto::decode64AndDecrypt)
                .map(String::new)
                .peek(sessionId -> log.debug("Session has id {}", sessionId))
                .map(SubscriberId::new);
    }

    private Validation<String, Subscriber> resolveSubscriber(final Validation<String, SubscriberId> result) {
        return result
                .flatMap(id -> core.findSubscriber(id).toValidation(() -> "No such subscriber"));
    }

    public abstract void handle(final RoutingContext routingContext, final Subscriber subscriber);
}
