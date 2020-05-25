package so.blacklight.blacksound.web.handler;

import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.subscriber.Subscriber;
import so.blacklight.blacksound.subscriber.SubscriberId;

import java.util.Optional;

public class StatusHandler implements VertxHandler {

    private final StreamingCore core;

    public StatusHandler(final StreamingCore core) {
        this.core = core;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        final String maybeSubscriberId = routingContext.session().get(SESSION_KEY);

        final var response = Optional.ofNullable(maybeSubscriberId)
                .map(SubscriberId::new)
                .flatMap(core::findSubscriber)
                .map(this::authenticatedResponse)
                .orElseGet(this::unauthenticatedResponse);

        routingContext.response().end(asJson(response));
    }

    private StatusResponse authenticatedResponse(final Subscriber subscriber) {
        return new AuthenticatedStatusResponse(subscriber.getId().toString(), subscriber.isEnabled());
    }

    private StatusResponse unauthenticatedResponse() {
        return new UnauthenticatedStatusResponse(core.getAuthorizationURI().toASCIIString());
    }

    /**
     * Base class for supplying mandatory response parameters
     */
    static abstract class StatusResponse {

        public final boolean hasSession;
        public final String status;

        public StatusResponse(final boolean hasSession) {
            status = "ok";
            this.hasSession = hasSession;
        }
    }

    static class UnauthenticatedStatusResponse extends StatusResponse {

        public final String redirectUri;

        public UnauthenticatedStatusResponse(final String redirectUri) {
            super(false);

            this.redirectUri = redirectUri;
        }
    }

    static class AuthenticatedStatusResponse extends StatusResponse {

        public final String name;
        public final boolean streamingEnabled;

        public AuthenticatedStatusResponse(final String name, final boolean streamingEnabled) {
            super(true);

            this.name = name;
            this.streamingEnabled = streamingEnabled;
        }

    }
}
