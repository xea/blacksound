package so.blacklight.blacksound.config;

import io.vavr.control.Validation;

import java.io.InputStream;

public interface ConfigLoader {

    Validation<ConfigError, ServerConfig> load();
    Validation<ConfigError, ServerConfig> load(InputStream inputStream);

    static ConfigLoader getDefaultLoader() {
        return new JSONConfigLoader();
    }
}
