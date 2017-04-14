/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.plugins;

import java.io.File;

/**
 * The PluginPaths interface is a Paths interface that provides access
 * to the various paths required by the plugin system.
 */
public interface PluginPaths
{
    /**
     * The internal plugin storage directory is the base directory for the
     * infrastructure plugins.  These plugins are bound to a specific version of
     * Pulse and as such can not be upgraded independently.  
     *
     * @return the internal plugin storage directory.
     */
    File getInternalPluginStorageDir();

    /**
     * The plugin storage directory is the directory into which non-internal plugins
     * are stored.
     * <p/>
     * To manually install a plugin, you need to copy it into this directory.
     * <p/>
     * All pre-packaged plugins are copied into this directory before being deployed. 
     * <p/>
     * This is located within the PULSE_DATA to allow it to persist through Pulse upgrades.
     *
     * @return the plugin storage directory.
     */
    File getPluginStorageDir();

    /**
     * The plugin working directory is a temporary work space used by the Plugin system.
     * For example, when a Plugin is downloaded from a remote location, it is downloaded
     * into this directory before being deployed and moved to the plugin storage directory.
     * <p/>
     * Upgrades for plugins are also stored in this directory until a server restart allows
     * for the upgrade to be deployed.
     *
     * @return the directory used by the plugin system for temporary files.
     */
    File getPluginWorkDir();

    /**
     * The Plugin registry directory is the directory that contains the plugin registry
     * file.
     *
     * @return the plugin registry base directory
     */
    File getPluginRegistryDir();

    /**
     * The OSGI configuration directory is the directory used by equinox to maintain
     * its persistent configurations.  The contents of this directory are managed by
     * Equinox.
     *
     * @return the OSGI configuration directory.
     */
    File getOsgiConfigurationDir();

    /**
     * The prepackage plugin storage directory is the directory located within the Pulse
     * distribution that contains the latest versions of the plugins that are shipped with
     * Pulse.
     *
     * @return the repackaged plugin directory.
     */
    File getPrepackagedPluginStorageDir();
}
