package com.zutubi.pulse.web.project;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.tove.config.project.BuildOptionsConfiguration;
import com.zutubi.pulse.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.tove.config.project.ProjectConfigurationActions;

import java.io.File;

/**
 */
public class BrowseProjectDirAction extends BuildActionBase
{
    private String separator;
    private BuildOptionsConfiguration buildOptions;

    public BuildOptionsConfiguration getBuildOptions()
    {
        return buildOptions;
    }

    public String getSeparator()
    {
        return separator;
    }

    public String execute() throws Exception
    {
        BuildResult buildResult = getRequiredBuildResult();
        accessManager.ensurePermission(ProjectConfigurationActions.ACTION_VIEW_SOURCE, buildResult.getProject());

        // this value is going to be written to the vm template and evaluated by javascript, so
        // we need to ensure that we escape the escape char.
        separator = File.separator.replace("\\", "\\\\");

        // provide some useful feedback on why the working directory is not available.
        ProjectConfiguration projectConfig = getProject().getConfig();
        // a) the working copy is not being retained.
        buildOptions = projectConfig.getOptions();
        // b) else, the working directory has been cleaned up by a the projects "cleanup rules" or
        //    it has been manually deleted or the working directory capture has failed.

        return SUCCESS;
    }
}
