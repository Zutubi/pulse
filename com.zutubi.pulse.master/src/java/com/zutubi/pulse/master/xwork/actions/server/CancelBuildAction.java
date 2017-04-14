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

package com.zutubi.pulse.master.xwork.actions.server;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.ajax.SimpleResult;
import org.springframework.security.access.AccessDeniedException;

/**
 * Ajax action to cancel a running build.
 */
public class CancelBuildAction extends ActionSupport
{
    private static final Messages I18N = Messages.getInstance(CancelBuildAction.class);
    
    private long buildId;
    private boolean kill;
    private SimpleResult result;
    private BuildManager buildManager;

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public void setKill(boolean kill)
    {
        this.kill = kill;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    public String execute() throws Exception
    {
        String user = getPrinciple();
        String reason = user == null ? null : "requested by '" + user + "'";
        try
        {
            if (buildId == -1)
            {
                buildManager.terminateAllBuilds(reason, kill);
                result = new SimpleResult(true, I18N.format("all.termination.requested"));
            }
            else
            {
                BuildResult build = buildManager.getBuildResult(buildId);
                if (build == null)
                {
                    result = new SimpleResult(false, "Unknown build '" + buildId + "'");
                }
                else
                {
                    buildManager.terminateBuild(build, reason, kill);
                    result = new SimpleResult(true, I18N.format("termination.requested"));
                }
            }

            pauseForDramaticEffect();
        }
        catch (AccessDeniedException e)
        {
            result = new SimpleResult(false, I18N.format("termination.not.permitted"));
        }

        return SUCCESS;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
