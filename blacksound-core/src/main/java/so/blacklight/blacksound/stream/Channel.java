package so.blacklight.blacksound.stream;

import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Channel {

    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(4);
    private final Deque<Song> queue = new ConcurrentLinkedDeque<>();
    private final Consumer<Song> trackChangeListener;
    private final Logger log = LogManager.getLogger(getClass());

    private Song currentTrack;
    private Instant currentTrackStartTime;

    public Channel(final Consumer<Song> trackChangeListener) {
        this.trackChangeListener = trackChangeListener;
    }

    public boolean queueTrack(final Song song) {
        if (queue.isEmpty() && currentTrack == null) {
            log.info("Playlist is empty, playing song {}", song.getFullTitle());
            playTrack(song);
        } else {
            queue.addLast(song);
        }

        return true;
    }

    private void playTrack(final Song song) {
        currentTrackStartTime = Instant.now();

        currentTrack = song;

        scheduler.schedule(this::nextSong, song.getLength(TimeUnit.SECONDS), TimeUnit.SECONDS);

        trackChangeListener.accept(currentTrack);
    }

    private void nextSong() {
        final var nextTrack = queue.pollFirst();

        if (Objects.nonNull(nextTrack)) {
            log.info("Playing next song: {}", nextTrack.getFullTitle());
            playTrack(nextTrack);
        } else {
            log.info("Playlist exhausted, stopping stream");
            currentTrack = null;
        }
    }

    public Song currentTrack() {
        return currentTrack;
    }

    public List<Song> getPlaylist() {
        final var current = Option.of(currentTrack)
                .map(Collections::singletonList)
                .getOrElse(Collections.emptyList());

        final var playlist = new ArrayList<>(current);

        playlist.addAll(queue);

        return playlist;
    }

    public int getPlaybackPosition() {
        return Optional.ofNullable(currentTrackStartTime)
                .map(startTime -> Duration.between(startTime, Instant.now()))
                .map(Duration::getSeconds)
                .map(Long::intValue)
                .orElse(0);
    }
}
