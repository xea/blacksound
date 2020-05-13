/**
 * This package contains classes that allow for configuring the application externally, eg. using configuration files.
 *
 * The main configuration class is {@code ServerConfig} which contains all its relevant sub-configurations, like network
 * or thread pool settings.
 *
 * An instance of {@code ServerConfig} can be obtained using implementations of {@code ConfigLoader}, eg {@code JSONConfigLoader}.
 */
package so.blacklight.blacksound.config;