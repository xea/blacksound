package so.blacklight.blacksound.config;

import io.vavr.control.Option;

public class ServerConfig {

    private final NetworkConfig network;

    private final SpotifyConfig spotify;

    public ServerConfig() {
        network = null;
        spotify = null;
    }

    public NetworkConfig getNetworkConfig() {
        return Option.of(network).getOrElse(NetworkConfig::new);
    }

    public SpotifyConfig getSpotifyConfig() {
        return Option.of(spotify).getOrElse(SpotifyConfig::new);
    }
}
