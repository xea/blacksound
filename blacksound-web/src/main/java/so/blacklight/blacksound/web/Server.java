package so.blacklight.blacksound.web;

import io.vavr.control.Try;
import io.vavr.control.Validation;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.config.ConfigLoader;
import so.blacklight.blacksound.config.NetworkConfig;
import so.blacklight.blacksound.config.ServerConfig;
import so.blacklight.blacksound.crypto.AeadCrypto;
import so.blacklight.blacksound.web.handler.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;

public class Server {

    private static final Logger log = LogManager.getLogger(Server.class);

    private final Vertx vertx;
    private final HttpServer httpServer;
    private final CountDownLatch shutDownLatch;
    private final StreamingCore core;

    public Server(final ServerConfig serverConfig) {
        final var networkConfig = serverConfig.getNetworkConfig();
        final var spotifyConfig = serverConfig.getSpotifyConfig();

        // Prepare the Vert.x instance
        final var vertxOptions = new VertxOptions().setWorkerPoolSize(networkConfig.getWorkerPoolSize());

        core = new StreamingCore(spotifyConfig);
        vertx = Vertx.vertx(vertxOptions);

        // Prepare the HTTP server
        final var httpServerOptions = new HttpServerOptions()
                .setPort(networkConfig.getListenPort());

        final var routes = setupRoutes(networkConfig);

        httpServer = vertx.createHttpServer(httpServerOptions).requestHandler(routes);

        shutDownLatch = new CountDownLatch(1);
    }

    private Router setupRoutes(final NetworkConfig networkConfig) {
        final var router = Router.router(vertx);

        final var crypto = AeadCrypto.getInstance();

        final var sessionHandler = SessionHandler.create(LocalSessionStore.create(vertx))
                .setCookieHttpOnlyFlag(true)
                .setCookieSecureFlag(true)
                .setCookieSameSite(CookieSameSite.STRICT);

        router.route()
                .handler(LoggerHandler.create())
                .handler(sessionHandler)
                .handler(RoutingContext::next);

        router.route("/").handler(StaticHandler.create());
        router.route("/favicon.ico").handler(FaviconHandler.create());
        router.route("/static/*").handler(StaticHandler.create());
        // This is where Spotify will call back once the authentication is done, registers new users without a session
        router.route("/spotify-redirect").handler(new CallbackHandler(core, vertx, crypto, networkConfig));
        // Handle user subscribe/unsubscribe requests coming from pre-registered users with live sessions
        router.route("/api/subscribe").handler(new SubscribeHandler(core, vertx));
        router.route("/api/unsubscribe").handler(new UnsubscribeHandler(core, vertx));
        router.route("/api/logout").handler(new LogoutHandler(core, vertx, crypto));

        router.route("/api/next-song").handler(new NextSongHandler());
        router.route("/api/play").handler(BodyHandler.create()).handler(new PlayHandler(core, vertx));
        router.route("/api/pause").handler(new PauseHandler(core, vertx));
        router.route("/api/queue").handler(BodyHandler.create()).handler(new QueueHandler(core, vertx, crypto));
        router.route("/api/playlist").handler(new PlaylistHandler(core, vertx));
        router.route("/api/resume").handler(new ResumeHandler(core, vertx, crypto));
        router.route("/api/search").handler(BodyHandler.create()).handler(new SearchHandler(core, vertx));
        // This exposes our redirect URI to the frontend
        router.route("/api/redirect-uri").handler(new RedirectURIHandler(core));
        router.route("/api/status").handler(new StatusHandler(core, vertx, crypto));


        // TODO we'll need to hide this call behind an authorization check once we've got users
        router.route("/api/shutdown").handler(new ShutDownHandler(this));

        return router;
    }

    public Validation<ServerError, ServerHandle> start() {
        log.info("Listening on port {}", httpServer.actualPort());
        httpServer.listen();

        log.info("Listening on port {}", httpServer.actualPort());

        return Validation.valid(new ServerHandle(shutDownLatch));
    }

    public static void main(String[] args) {
        log.info("Starting server");

        final var configLoader = ConfigLoader.getDefaultLoader();

        final var serverHandle = Try.of(() -> new FileInputStream(new File("config.json")))
                .toValidation()
                // Attempt to load external config first, using the above input stream
                .map(configLoader::load)
                .mapError(Server::logFallback)
                // Failing that, fall back to default (this load() is the no-argument version)
                .getOrElse(configLoader::load)
                .map(Server::new)
                .mapError(ServerError::from)
                .flatMap(Server::start);

        serverHandle.peek(handle -> {
            log.info("Server started");

            handle.join();
        });

        if (serverHandle.isInvalid()) {
            // Consider implementing more verbose error reporting
            log.error("Failed to start server: {}", serverHandle.getError().getMessage());
        }
    }

    private static Throwable logFallback(Throwable throwable) {
        log.debug("Failed to load configuration, falling back. Reason: {}", throwable.getMessage());

        return throwable;
    }


    public void shutDown() {
        vertx.close();
        shutDownLatch.countDown();
    }
}
