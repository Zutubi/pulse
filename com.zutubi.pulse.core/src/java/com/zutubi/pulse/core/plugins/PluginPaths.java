package com.zutubi.pulse.core.plugins;

import java.io.File;

/**
 *
 *
 */
public interface PluginPaths
{
    File getInternalPluginStorageDir();

    File getPluginStorageDir();

    File getPluginWorkDir();

    File getPluginRegistryDir();

    File getOsgiConfigurationDir();

    File getPrepackagedPluginStorageDir();
}
