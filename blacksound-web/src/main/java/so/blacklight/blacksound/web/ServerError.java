package so.blacklight.blacksound.web;

import so.blacklight.blacksound.config.ConfigError;

public class ServerError {

    private final String message;

    public ServerError(final String message) {
        this.message = message;
    }

    public static ServerError from(final ConfigError configError) {
        return new ServerError("Configuration error: " + configError.getMessage());
    }

    public String getMessage() {
        return message;
    }
}
