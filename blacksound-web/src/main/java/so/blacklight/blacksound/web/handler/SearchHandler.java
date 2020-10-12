package so.blacklight.blacksound.web.handler;

import com.google.gson.Gson;
import com.wrapper.spotify.model_objects.specification.Track;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.crypto.Crypto;
import so.blacklight.blacksound.subscriber.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SearchHandler extends AuthenticatedHandler {

    public SearchHandler(final StreamingCore core, final Vertx vertx, final Crypto crypto) {
        super(core, vertx, crypto);
    }

    @Override
    public void handle(RoutingContext routingContext, Subscriber subscriber) {
        final var request = new Gson().fromJson(routingContext.getBodyAsString(), SearchRequest.class);
        final var result = core.lookupSongs(request.searchExpression);
        final var response = routingContext.response();

        response.end(asJson(new SearchResponse(result.stream().map(Track::getName).collect(Collectors.toList()))));
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
