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

package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.servercore.api.AdminTokenManager;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;

/**
 * A task that creates the admin token used by pulse command line tools to
 * authenticate with the remote API.  This task is run after we bind the webapp
 * socket - when we're pretty confident we're not going to collide with another
 * running Pulse instance.
 */
public class AdminTokenStartupTask implements StartupTask
{
    private AdminTokenManager adminTokenManager;

    public void execute()
    {
        adminTokenManager.init();
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void setAdminTokenManager(AdminTokenManager adminTokenManager)
    {
        this.adminTokenManager = adminTokenManager;
    }
}