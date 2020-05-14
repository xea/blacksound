package so.blacklight.blacksound.config;

import io.vavr.control.Option;
import so.blacklight.blacksound.spotify.SpotifyConfig;

public class ServerConfig {

    private final NetworkConfig network;

    private final SpotifyConfig spotify;

    private ServerConfig() {
        network = null;
        spotify = null;
    }

    public NetworkConfig getNetworkConfig() {
        return Option.of(network).getOrElse(NetworkConfig::new);
    }

    public SpotifyConfig getSpotifyConfig() {
        return spotify;
    }
}
