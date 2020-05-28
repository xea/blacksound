package so.blacklight.blacksound.stream;

import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Song {

    private final String trackUri;

    private final String artist;

    private final String title;

    private final long length;

    public Song(final Track track) {
        this.trackUri = track.getUri();
        this.title = track.getName();
        this.length = track.getDurationMs() / 1000;
        this.artist = Arrays.stream(track.getArtists())
                .map(ArtistSimplified::getName)
                .collect(Collectors.joining(","));
    }

    public String getUri() {
        return trackUri;
    }

    public long getLength(final TimeUnit timeUnit) {
        return timeUnit.convert(Duration.of(length, ChronoUnit.SECONDS));
    }
}
