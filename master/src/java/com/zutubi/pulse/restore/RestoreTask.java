package com.zutubi.pulse.restore;

import com.zutubi.pulse.monitor.Task;

/**
 *
 *
 */
public interface RestoreTask extends Task
{
    ArchiveableComponent getComponent();
}
