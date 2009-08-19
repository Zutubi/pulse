package com.zutubi.pulse.master.xwork.actions.project;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraph;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.DependencyTransitiveMode;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Grid;
import com.zutubi.util.Mapping;
import com.zutubi.util.Pair;

import java.util.List;

/**
 * Action to display project dependencies page - upstream and downstream
 * dependency trees.
 */
public class ProjectDependenciesAction extends ProjectActionBase
{
    public static final String ANONYMOUS_MODE_KEY = "pulse.anonymousUserDependencyTransientMode";

    private ProjectDependencyGraphBuilder projectDependencyGraphBuilder;
    private Grid<ProjectDependencyData> upstream;
    private Grid<ProjectDependencyData> downstream;
    private String transitiveMode = DependencyTransitiveMode.SHOW_FULL_CASCADE.name();

    public Grid<ProjectDependencyData> getUpstream()
    {
        return upstream;
    }

    public Grid<ProjectDependencyData> getDownstream()
    {
        return downstream;
    }

    public String getTransitiveMode()
    {
        return transitiveMode;
    }

    public ProjectHealth getHealth(Project project)
    {
        return ProjectHealth.fromLatestBuilds(buildManager.getLatestCompletedBuildResults(project, 1));
    }

    public List<Pair<String, String>> getModes()
    {
        return CollectionUtils.map(DependencyTransitiveMode.values(), new Mapping<DependencyTransitiveMode, Pair<String, String>>()
        {
            public Pair<String, String> map(DependencyTransitiveMode mode)
            {
                return CollectionUtils.asPair(mode.name(), mode.name().toLowerCase().replace('_', ' '));
            }
        });
    }
    
    public String execute()
    {
        User user = getLoggedInUser();
        DependencyTransitiveMode mode;
        if (user == null)
        {
            mode = (DependencyTransitiveMode) ActionContext.getContext().getSession().get(ANONYMOUS_MODE_KEY);
            if (mode == null)
            {
                mode = DependencyTransitiveMode.SHOW_FULL_CASCADE;
            }
        }
        else
        {
            mode = user.getPreferences().getDependencyTransitiveMode();
        }

        transitiveMode = mode.name();

        Project project = getRequiredProject();
        ProjectDependencyGraph dependencyGraph = projectDependencyGraphBuilder.build(project, mode.getCorrespondingMode());
        ProjectDependencyGraphRenderer renderer = new ProjectDependencyGraphRenderer();
        upstream = renderer.renderUpstream(dependencyGraph);
        downstream = renderer.renderDownstream(dependencyGraph);

        return SUCCESS;
    }

    public void setProjectDependencyGraphBuilder(ProjectDependencyGraphBuilder projectDependencyGraphBuilder)
    {
        this.projectDependencyGraphBuilder = projectDependencyGraphBuilder;
    }
}
