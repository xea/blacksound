package so.blacklight.blacksound.subscriber;

import java.util.function.Consumer;

public interface SubscriberStore {

    void register(Subscriber subscriber);

    void forEach(Consumer<Subscriber> subscriberConsumer);
}
