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

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.util.StringUtils;

/**
 * Helper base class for actions under:
 *   browse/projects/&lt;project name&gt;/builds/&lt;build id&gt;/ <b>or</b>
 *   my/&lt;build id &gt;/
 */
public class BuildActionBase extends ProjectActionBase
{
    private boolean personal;
    private String buildVID;
    private BuildResult buildResult;
    private Viewport viewport;

    public boolean isPersonal()
    {
        return personal;
    }

    public void setPersonal(boolean personal)
    {
        this.personal = personal;
    }

    public String getBuildVID()
    {
        return buildVID;
    }

    public void setBuildVID(String buildVID)
    {
        this.buildVID = buildVID;
    }

    public long getBuildId()
    {
        BuildResult result = getBuildResult();
        if (result == null)
        {
            return 0;
        }
        else
        {
            return result.getId();
        }
    }
    
    public BuildResult getBuildResult()
    {
        if (buildResult == null)
        {
            if (isPersonal())
            {
                User user = getLoggedInUser();
                if(user == null)
                {
                    throw new LookupErrorException("Cannot look up personal builds when not logged in");
                }

                if(StringUtils.stringSet(buildVID))
                {
                    buildResult = buildManager.getByUserAndVirtualId(user, buildVID);
                    if(buildResult == null)
                    {
                        throw new LookupErrorException("Unknown personal build [" + buildVID + "]");
                    }

                    setProjectName(buildResult.getProject().getName());
                }
            }
            else
            {
                Project project = getProject();
                if (project != null)
                {
                    if (StringUtils.stringSet(buildVID))
                    {
                        buildResult = buildManager.getByProjectAndVirtualId(project, buildVID);
                        if(buildResult == null)
                        {
                            throw new LookupErrorException("Unknown build [" + buildVID + "] for project [" + project.getName() + "]");
                        }
                    }
                }
            }
        }

        return buildResult;
    }

    public BuildResult getRequiredBuildResult()
    {
        BuildResult result = getBuildResult();
        if(result == null)
        {
            throw new LookupErrorException("Build number required");
        }

        return buildResult;
    }

    public Viewport getViewport()
    {
        if (viewport == null)
        {
            viewport = loadBuildNavViewport(getBuildId());
        }
        return viewport;
    }
}
