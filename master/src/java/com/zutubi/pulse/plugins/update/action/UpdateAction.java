package com.zutubi.pulse.plugins.update.action;

/**
 */
public interface UpdateAction
{
    int getUnitsOfWork();
    UpdateResult execute(UpdateMonitor monitor);
}
