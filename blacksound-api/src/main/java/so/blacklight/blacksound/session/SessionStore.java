package so.blacklight.blacksound.session;

import java.util.Optional;

public interface SessionStore {

    Optional<Session> getSession(final SessionId sessionId);

    /**
     * Retrieve a session by its session ID or create a new session.
     *
     * @param sessionId the ID of the session to be loaded or created
     * @return the requested session
     */
    Session getOrCreateSession(final SessionId sessionId);
}
