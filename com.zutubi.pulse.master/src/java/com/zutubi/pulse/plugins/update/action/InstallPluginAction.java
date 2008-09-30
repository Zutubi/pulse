package com.zutubi.pulse.plugins.update.action;

/**
 */
public class InstallPluginAction implements UpdateAction
{
    
    public int getUnitsOfWork()
    {
        // Return the number of bytes to download??
        return 0;
    }

    public UpdateResult execute(UpdateMonitor monitor)
    {
        // Download the plugin.
        return null;
    }
}
