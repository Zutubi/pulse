package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.project.BuildStageModel;

/**
 * JSON model for executing build stages.  Extends the regular stage model with
 * information about the build the stage is within, as these models are shown
 * alone (not as part of a full build).
 */
public class ExecutingStageModel extends BuildStageModel
{
    private long number;
    private boolean personal;
    private String project;
    private String owner;
    
    public ExecutingStageModel(BuildResult buildResult, RecipeResultNode stageResult, Urls urls)
    {
        super(buildResult, stageResult, urls, false);

        number = buildResult.getNumber();
        personal = buildResult.isPersonal();
        project = buildResult.getProject().getName();
        owner = buildResult.getOwner().getName();
    }

    public long getNumber()
    {
        return number;
    }

    public boolean isPersonal()
    {
        return personal;
    }

    public String getProject()
    {
        return project;
    }

    public String getOwner()
    {
        return owner;
    }
    
    public String getBuildLink()
    {
        return getLink();
    }
}
