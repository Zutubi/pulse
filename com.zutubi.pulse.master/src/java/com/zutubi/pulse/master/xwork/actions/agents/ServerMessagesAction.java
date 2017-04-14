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

package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.tove.security.AccessManager;

/**
 * Action for the server and agent messages tabs.
 */
public class ServerMessagesAction extends AgentActionBase
{
    private int startPage;

    public int getStartPage()
    {
        return startPage;
    }

    public void setStartPage(int startPage)
    {
        this.startPage = startPage;
    }

    @Override
    public String execute() throws Exception
    {
        accessManager.ensurePermission(AccessManager.ACTION_ADMINISTER, null);

        try
        {
            Agent agent = getAgent();
            if (agent != null && !agent.isOnline())
            {
                addActionError("Agent is not online.");
            }
        }
        catch (LookupErrorException e)
        {
            addActionError(e.getMessage());
        }

        return SUCCESS;
    }
}
