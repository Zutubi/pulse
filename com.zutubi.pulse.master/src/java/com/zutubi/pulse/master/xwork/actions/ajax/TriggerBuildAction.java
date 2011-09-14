package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.xwork.actions.project.ProjectActionBase;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.util.logging.Logger;

/**
 * Manually trigger a project build.
 *
 * If the projects build options indicate that we should prompt for more input,
 * then we return "prompt" without triggering a build.
 */
public class TriggerBuildAction extends ProjectActionBase
{
    private static final Logger LOG = Logger.getLogger(TriggerBuildAction.class);

    private ActionManager actionManager;

    private boolean rebuild = false;

    private SimpleResult result;

    public boolean isRebuild()
    {
        return rebuild;
    }

    public void setRebuild(boolean rebuild)
    {
        this.rebuild = rebuild;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    public String execute()
    {
        Project project = getRequiredProject();
        ProjectConfiguration projectConfig = project.getConfig();

        actionManager.ensurePermission(projectConfig.getConfigurationPath(), ProjectConfigurationActions.ACTION_TRIGGER);

        if (projectConfig.getOptions().getPrompt())
        {
            /** The front end should use the {@link com.zutubi.pulse.master.xwork.actions.project.EditBuildPropertiesAction} for such projects. */
            LOG.warning("Bare trigger action used for project with prompting enabled.");
        }

        if (rebuild)
        {
            actionManager.execute(ProjectConfigurationActions.ACTION_REBUILD, projectConfig, null);
        }
        else
        {
            actionManager.execute(ProjectConfigurationActions.ACTION_TRIGGER, projectConfig, null);
        }

        pauseForDramaticEffect();

        result = new SimpleResult(true, "project build requested");
        return SUCCESS;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }
}
