package com.zutubi.pulse.plugins.update.action;

/**
 */
public interface UpdateMonitor
{
    void started(UpdateAction action);
    void progress(int workUnitsDone);
    void completed(UpdateResult result);
    void checkCancelled() throws UpdateCancelException;
}
