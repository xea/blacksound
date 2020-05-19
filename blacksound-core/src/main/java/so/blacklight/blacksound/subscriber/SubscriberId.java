package so.blacklight.blacksound.subscriber;

import so.blacklight.blacksound.id.Id;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SubscriberId implements Id<Subscriber, UUID> {

    private final UUID id;

    public SubscriberId() {
        this.id = UUID.randomUUID();
    }

    public SubscriberId(final UUID subscriberId) {
        this.id = Optional.of(subscriberId).orElse(UUID.randomUUID());
    }

    public SubscriberId(final String subscriberId) {
        this(UUID.fromString(subscriberId));
    }

    @Override
    public UUID value() {
        assert Objects.nonNull(id) : "Session Id must never be null";

        return id;
    }

    @Override
    public String toString() {
        return "SessionId{id=" + id + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriberId subscriberId = (SubscriberId) o;
        return Objects.equals(id, subscriberId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
