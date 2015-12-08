package com.zutubi.pulse.servercore.bootstrap;

import com.zutubi.pulse.core.util.config.EnvConfig;

import java.io.File;
import java.util.Map;

/**
 * Provides access to core configuration information: paths and properties needed to boot the Pulse
 * data subsystem and web interface.  Higher-level configuration may be found within the data
 * subsystem.
 */
public interface ConfigurationManager
{
    String CORE_PROPERTY_PULSE_HOME_DIR = "pulse.home.dir";
    String CORE_PROPERTY_USER_HOME_DIR = "user.home.dir";
    String CORE_PROPERTY_PULSE_DATA_DIR = "pulse.data.dir";
    String CORE_PROPERTY_PULSE_CONFIG_FILE = "pulse.config.file";
    String CORE_PROPERTY_PULSE_BIND_ADDRESS = "pulse.bind.address";
    String CORE_PROPERTY_PULSE_WEBAPP_PORT = "pulse.webapp.port";
    String CORE_PROPERTY_PULSE_CONTEXT_PATH = "pulse.context.path";

    EnvConfig getEnvConfig();
    SystemConfiguration getSystemConfig();
    UserPaths getUserPaths();
    SystemPaths getSystemPaths();

    /**
     * @return the path to use to determine free disk space on the server (the
     *         default is the data directory, but the user may override it)
     */
    File getDiskSpacePath();

    /**
     * Returns a set of key-value pairs for all of the "core" pulse properties,
     * such as important paths, that are in use.  This is useful for reporting
     * to the end user.  See the CORE_PROPERTY_* contants for supported keys.
     * <p/>
     * Note specific implementations of this interface may also define their
     * own pairs, and thus their own key constants.
     *
     * @return core properties describing the configuration of this instance
     */
    Map<String, String> getCoreProperties();

    /**
     * Loads system properties from a system.properties file in the user config area.
     */
    void loadSystemProperties();
}
