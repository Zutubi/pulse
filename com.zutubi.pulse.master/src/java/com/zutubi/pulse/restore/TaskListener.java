package com.zutubi.pulse.restore;

/**
 *
 *
 */
public interface TaskListener
{
    void taskCompleted(RestoreTask task);

    void taskAborted(RestoreTask task);

    void taskFailed(RestoreTask task);
}
