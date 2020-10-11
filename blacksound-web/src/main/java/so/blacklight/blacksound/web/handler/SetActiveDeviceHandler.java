package so.blacklight.blacksound.web.handler;

import com.google.gson.Gson;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.crypto.Crypto;
import so.blacklight.blacksound.subscriber.Subscriber;

public class SetActiveDeviceHandler extends AuthenticatedHandler {

    public SetActiveDeviceHandler(StreamingCore core, Vertx vertx, Crypto crypto) {
        super(core, vertx, crypto);
    }

    @Override
    public void handle(final RoutingContext routingContext, final Subscriber subscriber) {
        final var request = new Gson().fromJson(routingContext.getBodyAsString(), SetActiveDeviceRequest.class);
        final var deviceId = request.deviceId;

        subscriber.setActiveDevice(deviceId);

        routingContext.response().end(asJson(new SetActiveDeviceResponse("ok")));
    }

    static class SetActiveDeviceRequest {

        private String deviceId;

        public SetActiveDeviceRequest(final String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceId() {
            return deviceId;
        }
    }

    static class SetActiveDeviceResponse {

        private String status;

        public SetActiveDeviceResponse(final String status) {
            this.status = status;
        }
    }
}
