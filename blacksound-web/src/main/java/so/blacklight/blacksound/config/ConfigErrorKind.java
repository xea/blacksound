package so.blacklight.blacksound.config;

public enum ConfigErrorKind {
    FILE_NOT_FOUND("File not found"),
    INVALID_CONTENT("Invalid content"),
    INTERNAL_EXCEPTION("Internal exception");

    private final String defaultMessage;

    ConfigErrorKind(final String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
