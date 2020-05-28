package so.blacklight.blacksound.web.handler;

import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.config.NetworkConfig;
import so.blacklight.blacksound.crypto.Crypto;

import java.net.URI;

/**
 * Accepts callbacks from Spotify when a user authorizes us and we are provided with an authorization code. We need to
 * use this code to send back to Spotify in exchange for an access token and refresh token that will allow us to act
 * on the user's behalf.
 */
public class CallbackHandler implements VertxHandler {

    private final Vertx vertx;
    private final StreamingCore core;
    private final Crypto crypto;

    private final URI applicationUri;

    private final Logger log = LogManager.getLogger(getClass());

    public CallbackHandler(final StreamingCore core, final Vertx vertx, final Crypto crypto, final NetworkConfig networkConfig) {
        this.core = core;
        this.vertx = vertx;
        this.crypto = crypto;
        this.applicationUri = URI.create(networkConfig.getApplicationUri());
    }

    @Override
    public void handle(RoutingContext routingContext) {
        final var request = routingContext.request();
        final var response = routingContext.response();

        final var code = request.getParam("code");

        core.requestAuthorisation(code)
                .handle(this::handleError)
                .thenAcceptAsync(result -> {
                    result.peek(credentials -> {
                        final var subscriberCookie = processAuthorization(credentials);

                        routingContext.addCookie(subscriberCookie);

                        response.setStatusCode(HttpStatus.SC_MOVED_TEMPORARILY);
                        response.putHeader(HttpHeaders.LOCATION, applicationUri.toASCIIString());
                        response.end(asJson(new RegistrationResponse("ok")));
                    });
        }, vertx.nettyEventLoopGroup());
    }

    private Validation<Throwable, AuthorizationCodeCredentials> handleError(final AuthorizationCodeCredentials credentials, final Throwable throwable) {
        return Option.of(throwable)
                .peek(error -> log.error("Error during requesting authorization code", error))
                .map(Validation::<Throwable, AuthorizationCodeCredentials>invalid)
                .getOrElse(() -> Validation.valid(credentials));
    }

    private Cookie processAuthorization(final AuthorizationCodeCredentials credentials) {
        final var id = core.register(credentials);

        log.info("Registered new subscriber with ID {}", id);

        // Store the subscriber id on in an encrypted cookie on the client side
        final var encryptedId = crypto.encryptAndEncode64(id.toString().getBytes());

        final var subscriberCookie = Cookie.cookie(SESSION_KEY, encryptedId);
        subscriberCookie.setHttpOnly(true);
        subscriberCookie.setSameSite(CookieSameSite.STRICT);

        return subscriberCookie;
    }

    private static class RegistrationResponse {
        public final String status;

        public RegistrationResponse(final String status) {
            this.status = status;
        }
    }
}
