package so.blacklight.blacksound.web.handler;

import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;

public class PauseResumeHandler implements VertxHandler {

    private final StreamingCore core;

    public PauseResumeHandler(final StreamingCore core) {
        this.core = core;
    }

    @Override
    public void handle(final RoutingContext routingContext) {
        core.pause();
    }
}
