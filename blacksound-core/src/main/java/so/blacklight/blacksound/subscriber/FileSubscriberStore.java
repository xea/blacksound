package so.blacklight.blacksound.subscriber;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vavr.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileSubscriberStore implements SubscriberStore {

    private static final String DEFAULT_STORE_FILE = "subscribers.json";
    private final Logger log = LogManager.getLogger(getClass());

    private final File storeFile;

    public FileSubscriberStore() {
        this(DEFAULT_STORE_FILE);
    }

    public FileSubscriberStore(final String storePath) {
        this(Path.of(Objects.requireNonNull(storePath)));
    }

    public FileSubscriberStore(final Path storePath) {
        this(new File(Objects.requireNonNull(storePath).toUri()));
    }

    public FileSubscriberStore(final File storeFile) {
        this.storeFile = Objects.requireNonNull(storeFile);
    }

    @Override
    public Set<SubscriberHandle> loadEntries() {
        final var storePath = storeFile.toPath();

        try (final var fileReader = Files.newBufferedReader(storePath)) {
            final var typeToken = new TypeToken<Set<SubscriberHandle>>() {};

            final Set<SubscriberHandle> rawResponse = new Gson().fromJson(fileReader, typeToken.getType());

            return Optional.ofNullable(rawResponse).orElse(Collections.emptySet());
        } catch (IOException e) {
            log.error("Error while loading subscribers from file store: {}", e.getMessage());
        }

        return Collections.emptySet();
    }

    @Override
    public <T> Set<T> loadEntries(Function<SubscriberHandle, T> handleMapper) {
        return loadEntries()
                .stream()
                .map(handleMapper)
                .collect(Collectors.toSet());
    }

    @Override
    public void saveEntries(final Collection<SubscriberHandle> subscribers) {
        try (final var fileWriter = Files.newBufferedWriter(storeFile.toPath())) {
            new Gson().toJson(subscribers, fileWriter);
        } catch (IOException e) {
            log.error("Error while saving subscribers to file store", e);
        }
    }
}
