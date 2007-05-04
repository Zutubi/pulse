package com.zutubi.pulse.web.project;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.Scope;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.*;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class EditExeActionAction extends AbstractEditPostBuildActionAction
{
    // Create it so webwork doesn't try to
    private RunExecutablePostBuildAction action = new RunExecutablePostBuildAction();
    private Scope exampleScope;
    private MasterConfigurationManager configurationManager;

    public boolean isExe()
    {
        return true;
    }

    public Scope getExampleScope()
    {
        return exampleScope;
    }

    public void prepare() throws Exception
    {
        super.prepare();
        if(hasErrors())
        {
            return;
        }

        PostBuildAction a = lookupAction();

        if(a == null)
        {
            return;
        }

        if (!(a instanceof RunExecutablePostBuildAction))
        {
            addActionError("Invalid post build action type '" + a.getType() + "'");
            return;
        }

        action = (RunExecutablePostBuildAction) a;

        List<BuildResult> lastBuild = buildManager.queryBuilds(new Project[] { getProject() }, new ResultState[] { ResultState.SUCCESS }, -1, -1, null, 0, 1, true);
        if(!lastBuild.isEmpty())
        {
            BuildResult result = lastBuild.get(0);
            List<RecipeResultNode> stages = result.getRoot().getChildren();
            RecipeResultNode node = null;

            if(isStage() && stages.size() > 0)
            {
                node = stages.get(0);
            }
            
            exampleScope = RunExecutablePostBuildAction.getScope(result, node, new LinkedList<ResourceProperty>(), configurationManager);
        }
    }

    public RunExecutablePostBuildAction getPostBuildAction()
    {
        return action;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
