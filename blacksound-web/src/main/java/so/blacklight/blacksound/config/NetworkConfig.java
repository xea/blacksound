package so.blacklight.blacksound.config;

public class NetworkConfig {
    private static final int DEFAULT_LISTEN_PORT = 9896;
    private static final int DEFAULT_WORKER_POOL_SIZE = 16;

    private final int listenPort;
    private final int workerPoolSize;

    public NetworkConfig() {
        listenPort = DEFAULT_LISTEN_PORT;
        workerPoolSize = DEFAULT_WORKER_POOL_SIZE;
    }

    public int getListenPort() {
        return listenPort;
    }

    public int getWorkerPoolSize() {
        return workerPoolSize;
    }
}
