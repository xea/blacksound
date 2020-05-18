package so.blacklight.blacksound.session.impl;

import so.blacklight.blacksound.Subscriber;
import so.blacklight.blacksound.session.SessionId;
import so.blacklight.blacksound.session.SessionStore;

import java.util.function.Consumer;

public class FileSessionStore implements SessionStore<Subscriber> {

    private final SessionStore<Subscriber> storeCache = new MemorySessionStore();

    @Override
    public SessionId add(final Subscriber session) {
        storeCache.add(session);
        return null;
    }

    @Override
    public void remove(final SessionId sessionId) {
        storeCache.remove(sessionId);
    }

    @Override
    public void forEach(final Consumer<Subscriber> sessionConsumer) {
        storeCache.forEach(sessionConsumer);
    }
}
