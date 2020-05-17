package so.blacklight.blacksound.session.impl;

import so.blacklight.blacksound.session.Session;
import so.blacklight.blacksound.session.SessionId;
import so.blacklight.blacksound.session.SessionStore;

import java.util.Optional;

public class MemorySessionStore implements SessionStore {

    @Override
    public Optional<Session> getSession(SessionId sessionId) {
        return Optional.empty();
    }

    @Override
    public Session getOrCreateSession(SessionId sessionId) {
        return null;
    }
}
