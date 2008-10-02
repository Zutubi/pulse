package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.master.monitor.Task;

/**
 *
 *
 */
public interface RestoreTask extends Task
{
    ArchiveableComponent getComponent();
}
