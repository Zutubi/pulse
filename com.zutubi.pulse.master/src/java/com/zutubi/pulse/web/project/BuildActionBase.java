package com.zutubi.pulse.web.project;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.web.LookupErrorException;
import com.zutubi.util.TextUtils;

/**
 * Helper base class for actions under:
 *   browse/projects/&lt;project name&gt;/builds/&lt;build id&gt;/ <b>or</b>
 *   dashboard/my/&lt;build id &gt;/
 */
public class BuildActionBase extends ProjectActionBase
{
    private boolean personal;
    private String buildVID;
    private BuildResult buildResult;

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

                if(TextUtils.stringSet(buildVID))
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
                    if (TextUtils.stringSet(buildVID))
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
}
