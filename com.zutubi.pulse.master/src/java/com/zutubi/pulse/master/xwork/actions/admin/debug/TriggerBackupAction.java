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

package com.zutubi.pulse.master.xwork.actions.admin.debug;

import com.zutubi.pulse.master.restore.BackupManager;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * Simple action to manually trigger the systems backup.
 */
public class TriggerBackupAction extends ActionSupport
{
    private BackupManager backupManager;

    public String execute() throws Exception
    {
        backupManager.triggerBackup();
        
        return super.execute();
    }

    public void setBackupManager(BackupManager backupManager)
    {
        this.backupManager = backupManager;
    }
}
