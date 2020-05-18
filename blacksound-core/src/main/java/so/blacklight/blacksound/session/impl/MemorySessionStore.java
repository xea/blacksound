package so.blacklight.blacksound.session.impl;

import so.blacklight.blacksound.Subscriber;
import so.blacklight.blacksound.session.SessionId;
import so.blacklight.blacksound.session.SessionStore;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class MemorySessionStore implements SessionStore<Subscriber> {

    private final List<Subscriber> subscribers = new CopyOnWriteArrayList<>();

    @Override
    public SessionId add(Subscriber subscriber) {
        subscribers.add(subscriber);

        return subscriber.getId();
    }

    @Override
    public void remove(SessionId sessionId) {
        subscribers.removeIf(subscriber -> subscriber.getId().equals(sessionId));
    }

    @Override
    public void forEach(Consumer<Subscriber> sessionConsumer) {
        subscribers.forEach(sessionConsumer);
    }
}
