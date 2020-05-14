package so.blacklight.blacksound;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Channel {

    private final List<Subscriber> subscribers;

    public Channel() {
         subscribers = new CopyOnWriteArrayList<>();
    }

}
