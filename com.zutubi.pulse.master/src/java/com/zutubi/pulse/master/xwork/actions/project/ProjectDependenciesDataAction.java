package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.dependency.ProjectDependencyGraph;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;

/**
 * Action to display project dependencies page - upstream and downstream
 * dependency trees.
 */
public class ProjectDependenciesDataAction extends ProjectDependenciesAction
{
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
        model = new ProjectDepenenciesModel(renderer.renderUpstream(dependencyGraph), renderer.renderDownstream(dependencyGraph));
        
        return SUCCESS;
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
