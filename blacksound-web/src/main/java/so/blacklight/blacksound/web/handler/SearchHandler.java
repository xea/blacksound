package so.blacklight.blacksound.web.handler;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SearchHandler implements VertxHandler {

    private final StreamingCore core;
    private final Vertx vertx;

    public SearchHandler(final StreamingCore core, final Vertx vertx) {
        this.core = core;
        this.vertx = vertx;
    }

    @Override
    public void handle(final RoutingContext routingContext) {
        CompletableFuture.runAsync(() -> {
            final var result = new ArrayList<String>();
            result.add("Metallica / One");
            result.add("Slayer / Raining blood");
            result.add("Justin Bieber / You too");

            final var response = routingContext.response();

            response.end(asJson(new SearchResponse(result)));

        }, vertx.nettyEventLoopGroup());
    }

    private static class SearchRequest {

        public String searchExpression;

    }

    private static class SearchResponse {

        public List<String> result;

        public SearchResponse(final List<String> result) {
            this.result = result;
        }
    }
}
