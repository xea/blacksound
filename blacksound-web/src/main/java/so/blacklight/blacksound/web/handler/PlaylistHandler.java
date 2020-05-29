package so.blacklight.blacksound.web.handler;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.stream.Song;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlaylistHandler implements VertxHandler {

    private final StreamingCore core;
    private final Vertx vertx;

    public PlaylistHandler(final StreamingCore core, final Vertx vertx) {
        this.core = core;
        this.vertx = vertx;
    }

    @Override
    public void handle(final RoutingContext routingContext) {
        CompletableFuture.runAsync(() -> {

            final var playlist = core.getChannel()
                    .getPlaylist()
                    .stream()
                    .map(Song::getFullTitle)
                    .collect(Collectors.toList());

            final var response = routingContext.response();

            response.end(asJson(new PlaylistResponse("ok", playlist)));
        }, vertx.nettyEventLoopGroup());
    }

    private static class PlaylistResponse {

        public final String status;
        public final List<String> playlist;

        public PlaylistResponse(final String status, final List<String> playlist) {
            this.status = status;
            this.playlist = playlist;
        }
    }
}
