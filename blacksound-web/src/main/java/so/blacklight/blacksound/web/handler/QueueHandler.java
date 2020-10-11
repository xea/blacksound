package so.blacklight.blacksound.web.handler;

import com.google.gson.Gson;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.crypto.Crypto;
import so.blacklight.blacksound.stream.Song;
import so.blacklight.blacksound.subscriber.Subscriber;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class QueueHandler extends AuthenticatedHandler {

    public QueueHandler(final StreamingCore core, final Vertx vertx, final Crypto crypto) {
        super(core, vertx, crypto);
    }

    @Override
    public void handle(RoutingContext routingContext, Subscriber subscriber) {
        final var request = new Gson().fromJson(routingContext.getBodyAsString(), QueueRequest.class);

        final var queueResponse = subscriber.lookupSong(request.trackUri)
                .map(core::queue)
                .map(e -> new QueueResponse("ok", core.getChannel()
                        .getPlaylist()
                        .stream()
                        .map(Song::getPrettyTitle)
                        .collect(Collectors.toList())))
                .getOrElseGet(QueueResponse::new);

        routingContext.response().end(asJson(queueResponse));
    }

    private static class QueueRequest {

        private String trackUri;

        private List<String> playlist;
    }

    private static class QueueResponse {

        private final String status;

        private final List<String> playlist;

        public QueueResponse(final String status) {
            this(status, Collections.emptyList());
        }

        public QueueResponse(final String status, final List<String> playlist) {
            this.status = status;
            this.playlist = playlist;
        }
    }
}
