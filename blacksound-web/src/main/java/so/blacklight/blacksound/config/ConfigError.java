package so.blacklight.blacksound.config;

/**
 * Error type for configuration-related errors, including parse errors, unreadable files and schema mismatches
 */
public class ConfigError {

    private final ConfigErrorKind errorKind;

    private final String message;

    public ConfigError(ConfigErrorKind errorKind) {
        this(errorKind, errorKind.getDefaultMessage());
    }

    public ConfigError(final ConfigErrorKind errorKind, final String message) {
        this.errorKind = errorKind;
        this.message = message;
    }

    public ConfigError(Throwable throwable) {
        this(ConfigErrorKind.INTERNAL_EXCEPTION, throwable.getMessage());
    }


    public ConfigErrorKind getErrorKind() {
        return errorKind;
    }

    public String getMessage() {
        return message;
    }
}
