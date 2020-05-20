package so.blacklight.blacksound.web.handler;

import com.google.gson.Gson;
import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;

public class PlayHandler implements VertxHandler {

    private final StreamingCore core;

    public PlayHandler(final StreamingCore core) {
        this.core = core;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        final var body = routingContext.getBodyAsString();
        final var playRequest = new Gson().fromJson(body, PlayRequest.class);
        final var response = routingContext.response();


        response.putHeader("Content-type", "application/json");

        core.play(playRequest.trackUri);

        response.end("{ status: \"ok\" }");
    }

    private static class PlayRequest {

        private String trackUri;

    }
}
