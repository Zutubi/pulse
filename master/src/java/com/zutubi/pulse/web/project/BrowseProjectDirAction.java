package com.zutubi.pulse.web.project;

import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;

import java.io.File;

/**
 */
public class BrowseProjectDirAction extends ProjectActionSupport
{
    private long buildId;
    private BuildResult buildResult;
    private String separator;

    public long getBuildId()
    {
        return buildId;
    }

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public String getSeparator()
    {
        return separator;
    }

    public Project getProject()
    {
        if(buildResult != null)
        {
            return buildResult.getProject();
        }

        return null;
    }

    public String execute() throws Exception
    {
        buildResult = getBuildManager().getBuildResult(buildId);
        if (buildResult == null)
        {
            addActionError("Unknown build [" + buildId + "]");
            return ERROR;
        }

        getProjectManager().checkWrite(buildResult.getProject());

        // this value is going to be written to the vm template and evaluated by javascript, so
        // we need to ensure that we escape the escape char.
        separator = File.separator.replace("\\", "\\\\");

        return SUCCESS;
    }
}
