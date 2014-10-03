package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

/**
 * Configuration for managing the storage (disk usage etc) on an agent.
 */
@SymbolicName("zutubi.agentStorageConfig")
@Form(fieldOrder = {"dataDirectory", "outsideCleanupAllowed", "diskSpaceThresholdEnabled", "diskSpaceThresholdMib"})
public class AgentStorageConfiguration extends AbstractConfiguration
{
    private String dataDirectory = "$(data.dir)/agents/$(agent.handle)";
    private boolean outsideCleanupAllowed = false;
    @ControllingCheckbox(checkedFields = {"diskSpaceThresholdMib"})
    private boolean diskSpaceThresholdEnabled = false;
    private long diskSpaceThresholdMib = 128;

    public String getDataDirectory()
    {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory)
    {
        this.dataDirectory = dataDirectory;
    }

    public boolean isOutsideCleanupAllowed()
    {
        return outsideCleanupAllowed;
    }

    public void setOutsideCleanupAllowed(boolean outsideCleanupAllowed)
    {
        this.outsideCleanupAllowed = outsideCleanupAllowed;
    }

    public boolean isDiskSpaceThresholdEnabled()
    {
        return diskSpaceThresholdEnabled;
    }

    public void setDiskSpaceThresholdEnabled(boolean diskSpaceThresholdEnabled)
    {
        this.diskSpaceThresholdEnabled = diskSpaceThresholdEnabled;
    }

    public long getDiskSpaceThresholdMib()
    {
        return diskSpaceThresholdMib;
    }

    public void setDiskSpaceThresholdMib(long diskSpaceThresholdMib)
    {
        this.diskSpaceThresholdMib = diskSpaceThresholdMib;
    }
}
