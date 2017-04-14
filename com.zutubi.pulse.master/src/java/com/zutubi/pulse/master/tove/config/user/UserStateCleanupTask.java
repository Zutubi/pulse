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

package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.tove.config.DatabaseStateCleanupTaskSupport;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.tove.config.ToveRuntimeException;

/**
 * Cleans up the state associated with a deleted user.
 */
class UserStateCleanupTask extends DatabaseStateCleanupTaskSupport
{
    private UserConfiguration instance;
    private UserManager userManager;
    private BuildManager buildManager;

    public UserStateCleanupTask(UserConfiguration instance, UserManager userManager, BuildManager buildManager, TransactionContext transactionContext)
    {
        super(instance.getConfigurationPath(), transactionContext);
        this.instance = instance;
        this.userManager = userManager;
        this.buildManager = buildManager;
    }

    public void cleanupState()
    {
        User user = userManager.getUser(instance.getUserId());
        if (user != null)
        {
            // Wait for a running personal build to complete
            BuildResult build = buildManager.getLatestBuildResult(user);
            if(build != null && build.running())
            {
                throw new ToveRuntimeException("Unable to delete user: the user has a personal build in progress.");
            }

            // clean up any user responsibility
            userManager.clearAllResponsibilities(user);
            userManager.delete(user);
        }
    }
}
