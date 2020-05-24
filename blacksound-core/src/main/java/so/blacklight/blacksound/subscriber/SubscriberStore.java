package so.blacklight.blacksound.subscriber;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public interface SubscriberStore {

    Set<SubscriberHandle> loadEntries();

    <T> Set<T> loadEntries(Function<SubscriberHandle, T> handleMapper);

    void saveEntries(Collection<SubscriberHandle> subscribers);
}
