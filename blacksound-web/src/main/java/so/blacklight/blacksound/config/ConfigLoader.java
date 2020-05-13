package so.blacklight.blacksound.config;

import io.vavr.control.Validation;

public interface ConfigLoader {

    Validation<ConfigError, ServerConfig> load();

    static ConfigLoader getDefaultLoader() {
        return new JSONConfigLoader();
    }
}
