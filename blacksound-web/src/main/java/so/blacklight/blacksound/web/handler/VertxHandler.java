package so.blacklight.blacksound.web.handler;

import com.google.gson.Gson;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface VertxHandler extends Handler<RoutingContext> {

    default <T> String asJson(final T object) {
        return new Gson().toJson(object);
    }

}
