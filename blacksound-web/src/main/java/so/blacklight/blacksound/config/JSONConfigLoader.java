package so.blacklight.blacksound.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;

import java.io.InputStream;

public class JSONConfigLoader implements ConfigLoader {

    private static final String DEFAULT_CONFIG_LOCATION = "/config.json";
    private static final String DEFAULT_SCHEMA_LOCATION = "/config.schema.json";

    @Override
    public Validation<ConfigError, ServerConfig> load() {
        return Option.of(JSONConfigLoader.class.getResourceAsStream(DEFAULT_CONFIG_LOCATION))
                .flatMap(Option::of)
                .toValidation(new ConfigError(ConfigErrorKind.FILE_NOT_FOUND))
                .flatMap(this::toJson);
    }

    private Validation<ConfigError, String> validate(final String input) {
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        final JsonSchema schema = factory.getSchema(JSONConfigLoader.class.getResourceAsStream(DEFAULT_SCHEMA_LOCATION));

        final ObjectMapper mapper = new ObjectMapper();

        return Try.of(() -> mapper.readTree(input))
                .map(schema::validate)
                .toValidation(ConfigError::new)
                .flatMap(validationErrors -> {
                    if (validationErrors.isEmpty()) {
                        return Validation.valid(input);
                    } else {
                        return Validation.invalid(new ValidationError(validationErrors));
                    }
                });
    }

    private Validation<ConfigError, ServerConfig> toJson(final InputStream inputStream) {
        return Try.of(inputStream::readAllBytes)
                .toValidation(exception -> new ConfigError(ConfigErrorKind.INTERNAL_EXCEPTION, exception.getMessage()))
                .map(String::new)
                .flatMap(this::validate)
                .flatMap(input -> Try.of(() -> new Gson().fromJson(input, ServerConfig.class))
                        .toValidation(ConfigError::new));
    }

}
