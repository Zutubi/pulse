package com.zutubi.pulse.master.xwork.actions.project;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraph;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;

/**
 * Action to display project dependencies page - upstream and downstream
 * dependency trees.
 */
public class ProjectDependenciesDataAction extends ProjectActionBase
{
    public static final String ANONYMOUS_MODE_KEY = "pulse.anonymousUserDependencyTransientMode";

    private ProjectDependencyGraphBuilder projectDependencyGraphBuilder;
    private ProjectDepenenciesModel model;
    
    private ConfigurationManager configurationManager;

    public ProjectDepenenciesModel getModel()
    {
        return model;
    }

    public String execute()
    {
        ProjectDependencyGraphBuilder.TransitiveMode mode = lookupTransitiveMode();

        Project project = getRequiredProject();
        Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
        ProjectDependencyGraph dependencyGraph = projectDependencyGraphBuilder.build(project, mode);
        ProjectDependencyGraphRenderer renderer = new ProjectDependencyGraphRenderer(buildManager, urls);
        model = new ProjectDepenenciesModel(mode.name(), renderer.renderUpstream(dependencyGraph), renderer.renderDownstream(dependencyGraph));
        
        return SUCCESS;
    }
    
    private ProjectDependencyGraphBuilder.TransitiveMode lookupTransitiveMode()
    {
        User user = getLoggedInUser();
        ProjectDependencyGraphBuilder.TransitiveMode mode;
        if (user == null)
        {
            mode = (ProjectDependencyGraphBuilder.TransitiveMode) ActionContext.getContext().getSession().get(ANONYMOUS_MODE_KEY);
            if (mode == null)
            {
                mode = ProjectDependencyGraphBuilder.TransitiveMode.FULL;
            }
        }
        else
        {
            mode = user.getPreferences().getDependencyTransitiveMode();
        }

        return mode;
    }

    public void setProjectDependencyGraphBuilder(ProjectDependencyGraphBuilder projectDependencyGraphBuilder)
    {
        this.projectDependencyGraphBuilder = projectDependencyGraphBuilder;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
