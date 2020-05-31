package so.blacklight.blacksound.web.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractHandler implements VertxHandler {

    protected final Logger log = LogManager.getLogger(getClass());
}
