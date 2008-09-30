package com.zutubi.pulse.dev.bootstrap;

import com.zutubi.pulse.plugins.PluginPaths;

import java.io.File;

/**
 * Abstraction of paths used by dev packages.
 */
public interface DevPaths extends PluginPaths
{
    /**
     * @return a per-user directory for storing configuration and such
     * information.
     */
    File getUserRoot();
}
