package so.blacklight.blacksound.config;

public class NetworkConfig {
    private static final String DEFAULT_APPLICATION_URI = "https://localhost:9861/";
    private static final int DEFAULT_LISTEN_PORT = 9861;
    private static final int DEFAULT_WORKER_POOL_SIZE = 16;

    private final String applicationUri;
    private final int listenPort;
    private final int workerPoolSize;

    public NetworkConfig() {
        applicationUri = DEFAULT_APPLICATION_URI;
        listenPort = DEFAULT_LISTEN_PORT;
        workerPoolSize = DEFAULT_WORKER_POOL_SIZE;
    }

    public String getApplicationUri() {
        return applicationUri;
    }

    public int getListenPort() {
        return listenPort;
    }

    public int getWorkerPoolSize() {
        return workerPoolSize;
    }

}
