package so.blacklight.blacksound.web.handler;

import com.google.gson.Gson;
import com.wrapper.spotify.model_objects.miscellaneous.Device;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.crypto.Crypto;
import so.blacklight.blacksound.subscriber.Subscriber;

import java.util.List;

public class SetActiveDeviceHandler extends AuthenticatedHandler {

    public SetActiveDeviceHandler(StreamingCore core, Vertx vertx, Crypto crypto) {
        super(core, vertx, crypto);
    }

    @Override
    public void handle(final RoutingContext routingContext, final Subscriber subscriber) {
        final var request = new Gson().fromJson(routingContext.getBodyAsString(), SetActiveDeviceRequest.class);
        final var deviceId = request.deviceId;

        final var devices = subscriber.setActiveDevice(deviceId);

        routingContext.response().end(asJson(new SetActiveDeviceResponse("ok", devices)));
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

        private List<Device> devices;

        public SetActiveDeviceResponse(final String status, final List<Device> devices) {
            this.status = status;
            this.devices = devices;
        }
    }
}
