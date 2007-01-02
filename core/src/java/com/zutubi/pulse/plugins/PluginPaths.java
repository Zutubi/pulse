package com.zutubi.pulse.plugins;

import java.io.File;

/**
 * Defines locations on the file system where plugin jars/directories can be
 * found.
 */
public interface PluginPaths
{
    /**
     * @return the directory in which equinox configuration data is stored
     */
    File getPluginConfigurationRoot();

    /**
     * @return the directory under which all internal plugins are stored:
     * these are plugins that are required for Pulse to function, e.g. the
     * extension registry
     */
    File getInternalPluginRoot();

    /**
     * @return the directory under which pre-packaged plugins are stored:
     * these are plugins that are shipped with Pulse by default, but may be
     * disabled by the user if they choose
     */
    File getPrepackagedPluginRoot();

    /**
     * @return the directory under which user-installed plugins are stored.
     */
    File getUserPluginRoot();
}
