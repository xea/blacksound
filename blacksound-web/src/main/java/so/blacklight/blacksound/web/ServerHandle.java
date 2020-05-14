package so.blacklight.blacksound.web;

import io.vavr.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;

public class ServerHandle {

    private final CountDownLatch shutDownLatch;
    private final Logger log = LogManager.getLogger(getClass());

    public ServerHandle(CountDownLatch shutDownLatch) {
        this.shutDownLatch = shutDownLatch;
    }

    /**
     * Blocks the calling thread until the corresponding server thread has been shut down.
     */
    public void join() {
        Try.run(shutDownLatch::await).onFailure(throwable -> {
            log.error("Error while waiting for shut down", throwable);
        });
    }
}
