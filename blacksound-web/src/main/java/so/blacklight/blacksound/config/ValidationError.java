package so.blacklight.blacksound.config;

import com.networknt.schema.ValidationMessage;

import java.util.Set;
import java.util.stream.Collectors;

public class ValidationError extends ConfigError {

    private final Set<ValidationMessage> validationMessages;

    public ValidationError(final Set<ValidationMessage> validationMessages) {
        super(ConfigErrorKind.INVALID_CONTENT);

        this.validationMessages = validationMessages;
    }

    @Override
    public String getMessage() {
        return validationMessages.stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.joining());
    }
}
