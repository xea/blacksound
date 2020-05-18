package so.blacklight.blacksound.session;

import java.util.Optional;
import java.util.function.Consumer;

public interface SessionStore<T extends Session> {

    SessionId add(T session);

    void remove(SessionId sessionId);

    void forEach(Consumer<T> sessionConsumer);
}
