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

import com.zutubi.pulse.master.restore.BackupManager;
import com.zutubi.pulse.master.upgrade.UpgradeException;
import com.zutubi.util.logging.Logger;

/**
 * Trigger a backup before continuing with the upgrade.
 */
public class BackupUpgradeTask extends AbstractUpgradeTask 
{
    private static final Logger LOG = Logger.getLogger(BackupUpgradeTask.class);
    
    private BackupManager backupManager;

    public int getBuildNumber()
    {
        // -/ve indicates that this build number should not be recorded against the target data directory. 
        return -1;
    }

    public void execute() throws UpgradeException
    {
        try
        {
            backupManager.triggerBackup();
        }
        catch (Exception e)
        {
            LOG.severe(e);
            addError(e.getMessage());
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void setBackupManager(BackupManager backupManager)
    {
        this.backupManager = backupManager;
    }
}
