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

package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.bootstrap.Data;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.upgrade.ConfigurationAware;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.pulse.servercore.cleanup.FileDeletionService;

import java.io.File;

/**
 * Cleanup the existing ivy cache directories.  We have disabled caching so be
 * remain consistent we now cleanup any existing cache entries.
 */
public class CleanIvyCacheUpgradeTask extends AbstractUpgradeTask implements ConfigurationAware
{
    private MasterConfigurationManager configurationManager;
    private FileDeletionService fileDeletionService;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        Data data = configurationManager.getData();
        File cacheBase = new File(data.getData(), "cache");

        // this could take a while so schedule it in the background.
        fileDeletionService.delete(cacheBase, false, false);
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setFileDeletionService(FileDeletionService fileDeletionService)
    {
        this.fileDeletionService = fileDeletionService;
    }
}
