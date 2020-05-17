package so.blacklight.blacksound.session;

import so.blacklight.blacksound.Id;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SessionId implements Id<Session, UUID> {

    private final UUID id;

    public SessionId() {
        this.id = UUID.randomUUID();
    }

    public SessionId(final UUID sessionId) {
        this.id = Optional.ofNullable(sessionId).orElse(UUID.randomUUID());
    }

    @Override
    public UUID value() {
        assert Objects.nonNull(id) : "Session Id must never be null";

        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionId sessionId = (SessionId) o;
        return Objects.equals(id, sessionId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
