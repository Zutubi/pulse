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

package com.zutubi.pulse.master.xwork.actions.admin;

import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.security.AccessManager;

/**
 * Trivial action that serves as an entry point to the Pulse 3 admin application. The UI is all
 * controlled from the client side, talking to APIs.
 */
public class AdminAppAction extends ActionSupport
{
    private String path;
    private boolean admin;
    private boolean projectCreateAllowed;
    private boolean agentCreateAllowed;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public boolean isAdmin()
    {
        return admin;
    }

    public boolean isProjectCreateAllowed()
    {
        return projectCreateAllowed;
    }

    public boolean isAgentCreateAllowed()
    {
        return agentCreateAllowed;
    }

    @Override
    public String execute() throws Exception
    {
        admin = accessManager.hasPermission(AccessManager.ACTION_ADMINISTER, null);
        projectCreateAllowed = configurationSecurityManager.hasPermission(MasterConfigurationRegistry.PROJECTS_SCOPE, AccessManager.ACTION_CREATE);
        agentCreateAllowed = configurationSecurityManager.hasPermission(MasterConfigurationRegistry.AGENTS_SCOPE, AccessManager.ACTION_CREATE);
        return SUCCESS;
    }
}
