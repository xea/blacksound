package so.blacklight.blacksound.subscriber;

import java.util.function.Function;

public interface SubscriberStore {

    void register(Subscriber subscriber);

    int forEach(Function<Subscriber, Boolean> subscriberConsumer);

    void save();

    void restore();
}
