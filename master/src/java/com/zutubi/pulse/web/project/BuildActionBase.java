package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.web.LookupErrorException;

/**
 * Helper base class for actions under browse/projects/<projectName>/builds/<buildId>/
 */
public class BuildActionBase extends ProjectActionBase
{
    private String buildVID;
    private BuildResult buildResult;

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
        Project project = getProject();
        if (buildResult == null && project != null)
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
