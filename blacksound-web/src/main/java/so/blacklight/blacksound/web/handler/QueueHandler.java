package so.blacklight.blacksound.web.handler;

import com.google.gson.Gson;
import io.vavr.control.Option;
import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.crypto.Crypto;
import so.blacklight.blacksound.subscriber.SubscriberId;

import java.util.concurrent.CompletableFuture;

import static so.blacklight.blacksound.web.handler.AuthenticatedHandler.SESSION_KEY;

public class QueueHandler implements VertxHandler {

    private final StreamingCore core;
    private final Crypto crypto;
    private final Vertx vertx;
    private final Logger log = LogManager.getLogger(getClass());

    public QueueHandler(final StreamingCore core, final Vertx vertx, final Crypto crypto) {
        this.core = core;
        this.vertx = vertx;
        this.crypto = crypto;
    }

    @Override
    public void handle(final RoutingContext routingContext) {
        CompletableFuture.runAsync(() -> {
            final var request = new Gson().fromJson(routingContext.getBodyAsString(), QueueRequest.class);

            final var queueResponse = Option.of(routingContext.getCookie(SESSION_KEY))
                    .peek(cookie -> log.debug("Found session cookie"))
                    .map(Cookie::getValue)
                    .toValidation(() -> "Subscriber cookie was not found")
                    .flatMap(crypto::decode64AndDecrypt)
                    .map(String::new)
                    .peek(sessionId -> log.debug("Session has id {}", sessionId))
                    .map(SubscriberId::new)
                    .flatMap(id -> core.findSubscriber(id).toValidation(() -> "No such subscriber"))
                    .flatMap(subscriber -> subscriber.lookupSong(request.trackUri))
                    .map(core::queue)
                    .map(e -> new QueueResponse("ok"))
                    .getOrElseGet(QueueResponse::new);

            routingContext.response().end(asJson(queueResponse));
        }, vertx.nettyEventLoopGroup());
    }

    private static class QueueRequest {

        private String trackUri;

    }

    private static class QueueResponse {

        private final String status;

        public QueueResponse(final String status) {
            this.status = status;
        }
    }
}
