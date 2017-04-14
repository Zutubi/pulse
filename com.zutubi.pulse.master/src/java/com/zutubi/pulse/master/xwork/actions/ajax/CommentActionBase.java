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

package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.util.UnaryProcedure;

/**
 * Helper base class for actions that manipulate comments.
 */
public abstract class CommentActionBase extends ActionSupport
{
    private static final Messages I18N = Messages.getInstance(CommentActionBase.class);

    private long agentId;
    private long buildId;
    private SimpleResult result;
    private AgentManager agentManager;
    private BuildManager buildManager;

    public void setAgentId(long agentId)
    {
        this.agentId = agentId;
    }

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    @Override
    public String execute() throws Exception
    {
        try
        {
            final User user = getLoggedInUser();
            if (user == null)
            {
                throw new IllegalStateException(I18N.format("not.logged.in"));
            }

            if (agentId == 0)
            {
                BuildResult build = buildManager.getBuildResult(buildId);
                if (build == null)
                {
                    throw new IllegalArgumentException(I18N.format("unknown.build", buildId));
                }

                updateContainer(build, user);
                buildManager.save(build);
            }
            else
            {
                final Agent agent = agentManager.getAgentById(agentId);
                if (agent == null)
                {
                    throw new IllegalArgumentException(I18N.format("unknown.agent", agentId));
                }

                agentManager.updateAgentState(agent, new UnaryProcedure<AgentState>()
                {
                    public void run(AgentState agentState)
                    {
                        updateContainer(agentState, user);
                    }
                });
            }

            result = new SimpleResult(true, "");
        }
        catch (Exception e)
        {
            result = new SimpleResult(false, e.getMessage());
        }

        return SUCCESS;
    }

    protected abstract void updateContainer(CommentContainer container, User user);

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
