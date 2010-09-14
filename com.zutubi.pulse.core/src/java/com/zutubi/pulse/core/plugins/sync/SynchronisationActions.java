package com.zutubi.pulse.core.plugins.sync;

import com.zutubi.pulse.core.plugins.repository.PluginInfo;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Encapsulates the actions that are required to synchronise plugins with a
 * repository.
 */
public class SynchronisationActions
{
    private List<PluginInfo> toInstall = new LinkedList<PluginInfo>();
    private List<PluginInfo> toUpgrade = new LinkedList<PluginInfo>();
    private List<String> toUninstall = new LinkedList<String>();

    /**
     * Returns true if some action is required to synchronise.
     * 
     * @return true if at least one action is required to synchronise
     */
    public boolean isSyncRequired()
    {
        return toInstall.size() > 0 || toUpgrade.size() > 0 || toUninstall.size() > 0;
    }
    
    /**
     * Indicates if a reboot will be required to complete synchronisation.
     * 
     * @return true if a reboot would be required to synchronise
     */
    public boolean isRebootRequired()
    {
        return toUpgrade.size() > 0 || toUninstall.size() > 0;
    }

    /**
     * Gets information about plugins to install.
     * 
     * @return a list of plugins that need to be installed
     */
    public List<PluginInfo> getToInstall()
    {
        return Collections.unmodifiableList(toInstall);
    }

    /**
     * Adds a plugin to be installed to the actions.
     * 
     * @param info information for the plugin that needs to be installed
     */
    void addInstall(PluginInfo info)
    {
        toInstall.add(info);
    }

    /**
     * Gets information about plugins to upgrade.  The information returned
     * reflects the version to be upgraded to.
     * 
     * @return a list of plugins that need to be upgraded
     */
    public List<PluginInfo> getToUpgrade()
    {
        return Collections.unmodifiableList(toUpgrade);
    }

    /**
     * Adds a plugin to be upgraded to the actions.  The given information
     * should reflect the version to upgrade to.
     * 
     * @param info information for the plugin that needs to be upgraded
     */
    void addUpgrade(PluginInfo info)
    {
        toUpgrade.add(info);
    }
    
    /**
     * Gets information about plugins to uninstall.
     * 
     * @return a list of ids for plugins that need to be uninstalled
     */
    public List<String> getToUninstall()
    {
        return Collections.unmodifiableList(toUninstall);
    }

    /**
     * Adds a plugin to be removed to the actions.
     * 
     * @param id id of the plugin that needs to be uninstalled
     */
    void addUninstall(String id)
    {
        toUninstall.add(id);
    }
}
