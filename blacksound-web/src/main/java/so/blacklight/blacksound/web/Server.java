package so.blacklight.blacksound.web;

import io.vavr.control.Validation;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.StreamingCore;
import so.blacklight.blacksound.config.ConfigLoader;
import so.blacklight.blacksound.config.ServerConfig;
import so.blacklight.blacksound.web.handler.*;

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

        final var routes = setupRoutes();

        httpServer = vertx.createHttpServer(httpServerOptions).requestHandler(routes);

        shutDownLatch = new CountDownLatch(1);
    }

    private Router setupRoutes() {
        final var router = Router.router(vertx);

        final var sessionHandler = SessionHandler.create(LocalSessionStore.create(vertx))
                .setCookieHttpOnlyFlag(true)
                .setCookieSecureFlag(false) // TODO allow this once the keystores have been configured
                .setCookieSameSite(CookieSameSite.STRICT);

        router.route()
                .handler(LoggerHandler.create())
                .handler(sessionHandler)
                .handler(RoutingContext::next);

        router.route("/").handler(StaticHandler.create());
        router.route("/favicon.ico").handler(FaviconHandler.create());
        router.route("/static/*").handler(StaticHandler.create());
        router.route("/spotify-redirect").handler(new CallbackHandler(core, vertx));
        router.route("/api/next-song").handler(new NextSongHandler());
        router.route("/api/play").handler(new PlayHandler(core));
        router.route("/api/redirect-uri").handler(new RedirectURIHandler(core));
        router.route("/api/status").handler(new StatusHandler(core));

        // TODO we'll need to hide this call behind an authorization check once we've got users
        router.route("/api/shutdown").handler(new ShutDownHandler(this));

        return router;
    }

    public Validation<ServerError, ServerHandle> start() {
        httpServer.listen();

        log.info("Listening on port {}", httpServer.actualPort());

        return Validation.valid(new ServerHandle(shutDownLatch));
    }

    public static void main(String[] args) {
        log.info("Starting server");

        final var serverHandle = ConfigLoader.getDefaultLoader()
                .load()
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

    public void shutDown() {
        vertx.close();
        shutDownLatch.countDown();
    }
}
