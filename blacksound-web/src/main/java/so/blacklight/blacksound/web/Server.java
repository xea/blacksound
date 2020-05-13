package so.blacklight.blacksound.web;

import io.vavr.control.Validation;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import so.blacklight.blacksound.config.ConfigLoader;
import so.blacklight.blacksound.config.ServerConfig;
import so.blacklight.blacksound.web.handler.CallbackHandler;
import so.blacklight.blacksound.web.handler.NextSongHandler;

public class Server {

    private static final Logger log = LogManager.getLogger(Server.class);

    private final Vertx vertx;
    private final HttpServer httpServer;


    public Server(final ServerConfig serverConfig) {
        final var networkConfig = serverConfig.getNetworkConfig();

        // Prepare the Vert.x instance
        final var vertxOptions = new VertxOptions().setWorkerPoolSize(networkConfig.getWorkerPoolSize());

        vertx = Vertx.vertx(vertxOptions);

        // Prepare the HTTP server
        final var httpServerOptions = new HttpServerOptions()
                .setPort(networkConfig.getListenPort());

        final var routes = setupRoutes();

        httpServer = vertx.createHttpServer(httpServerOptions).requestHandler(routes);
    }

    private Router setupRoutes() {
        final var router = Router.router(vertx);

        router.route()
                .handler(LoggerHandler.create())
                .handler(RoutingContext::next);

        router.route("/").handler(StaticHandler.create());
        router.route("/favicon.ico").handler(FaviconHandler.create());
        router.route("/static/*").handler(StaticHandler.create());
        router.route("/spotify-callback").handler(new CallbackHandler());
        router.route("/api/next-song").handler(new NextSongHandler());

        return router;
    }

    public Validation<ServerError, ServerHandle> start() {
        httpServer.listen();

        return Validation.valid(null);
    }

    public static void main(String[] args) {
        log.info("Starting server");

        final var startupStatus = ConfigLoader.getDefaultLoader()
                .load()
                .map(Server::new)
                .mapError(ServerError::from)
                .flatMap(Server::start);

        if (startupStatus.isInvalid()) {
            // Consider implementing more verbose error reporting
            log.error("Failed to start server: {}", startupStatus.getError().getMessage());
        } else {
            log.info("Server started");
        }
    }

}
