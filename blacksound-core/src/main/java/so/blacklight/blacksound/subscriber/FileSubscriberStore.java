package so.blacklight.blacksound.subscriber;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileSubscriberStore implements SubscriberStore {

    private static final String DEFAULT_STORE_FILE = "subscribers.json";

    private final List<Subscriber> cache;

    private final File storeFile;

    private final Logger log = LogManager.getLogger(getClass());

    public FileSubscriberStore() {
        this(new File(DEFAULT_STORE_FILE).toURI());
    }

    public FileSubscriberStore(final URI storeUri) {
        this(new File(storeUri));
    }

    public FileSubscriberStore(final File storeFile) {
        cache = new CopyOnWriteArrayList<>();

        this.storeFile = storeFile;
        restore();
    }

    @Override
    public void restore() {
        try {
            final List<FileEntry> entries;

            synchronized (storeFile) {
                if (storeFile.exists()) {
                    final var inputStream = new FileInputStream(storeFile);
                    final var typeToken = new TypeToken<List<FileEntry>>() {};

                    // JSON parsing shouldn't really be in this synchronised block but meh, I'm lazy and this is not a
                    // critical section.
                    entries = new Gson().fromJson(new InputStreamReader(inputStream), typeToken.getType());
                } else {
                    if (!storeFile.createNewFile()) {
                        log.error("Could not create subscriber store file {} on load", storeFile.getAbsolutePath());
                    }

                    entries = new ArrayList<>();
                }
            }

            final var subscribers = Optional.ofNullable(entries)
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(FileEntry::toSubscriber)
                    .collect(Collectors.toList());

            synchronized (cache) {
                cache.clear();
                cache.addAll(subscribers);
            }

            log.info("Loaded {} subscribers from file store", subscribers.size());
        } catch (final FileNotFoundException e) {
            log.error("Could not load subscriber store from file {}", storeFile.getAbsolutePath(), e);
        } catch (IOException e) {
            log.error("Could not create empty subscriber store file {} on save", storeFile.getAbsolutePath(), e);
        }
    }

    @Override
    public void save() {
        try {
            final List<FileEntry> entries;

            synchronized (cache) {
                entries = cache.stream().map(FileEntry::new).collect(Collectors.toList());
            }

            final var json = new Gson().toJson(entries);

            synchronized (storeFile) {
                if (!storeFile.exists() && !storeFile.createNewFile()) {
                    log.error("Could not create new store file at {}", storeFile.getAbsolutePath());
                }

                final var fos = new FileOutputStream(storeFile);

                fos.write(json.getBytes());

                fos.flush();
                fos.close();
            }
        } catch (IOException e) {
            log.error("IO Error during persisting subscriber store", e);
        }
    }

    @Override
    public void register(final Subscriber subscriber) {
        cache.add(subscriber);

        save();
    }


    @Override
    public int forEach(Function<Subscriber, Boolean> subscriberConsumer) {
        return (int) cache.stream().map(subscriberConsumer).filter(p -> p).count();
    }

    private static class FileEntry {
        private String id;
        private String accessToken;
        private String refreshToken;
        private long expires;

        public FileEntry(final Subscriber subscriber) {
            this.id = subscriber.getId().value().toString();
            this.accessToken = subscriber.getApi().getAccessToken();
            this.refreshToken = subscriber.getApi().getRefreshToken();
            this.expires = subscriber.expires.getEpochSecond();
        }

        public Subscriber toSubscriber() {
            return new Subscriber(new SubscriberId(UUID.fromString(id)), accessToken, refreshToken, Instant.ofEpochSecond(expires));
        }
    }
}
