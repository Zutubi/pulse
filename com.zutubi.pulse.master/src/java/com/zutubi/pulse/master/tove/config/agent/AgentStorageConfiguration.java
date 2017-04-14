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
