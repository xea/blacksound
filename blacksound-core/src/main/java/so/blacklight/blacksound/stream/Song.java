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

    public Song(final String trackUri, final String artist, final String title, final long length) {
        this.trackUri = trackUri;
        this.artist = artist;
        this.title = title;
        this.length = length;
    }

    public String getUri() {
        return trackUri;
    }

    public long getLength(final TimeUnit timeUnit) {
        return timeUnit.convert(Duration.of(length, ChronoUnit.SECONDS));
    }

    public String getFullTitle() {
        return String.format("%s / %s", artist, title);
    }

    public String getPrettyTitle() {
        final var lenghtMinutes = length / 60;
        final var lengthSeconds = length % 60;

        return String.format("%s [%d:%02d]", getFullTitle(), lenghtMinutes, lengthSeconds);
    }
}
