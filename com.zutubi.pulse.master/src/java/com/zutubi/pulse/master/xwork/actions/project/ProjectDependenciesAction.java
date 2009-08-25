package com.zutubi.pulse.master.xwork.actions.project;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraph;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
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

    private static final Messages I18N = Messages.getInstance(ProjectDependenciesAction.class);

    private ProjectDependencyGraphBuilder projectDependencyGraphBuilder;
    private Grid<ProjectDependencyData> upstream;
    private Grid<ProjectDependencyData> downstream;
    private String transitiveMode = ProjectDependencyGraphBuilder.TransitiveMode.FULL.name();

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
        return ProjectHealth.fromLatestBuild(buildManager.getLatestCompletedBuildResult(project));
    }

    public List<Pair<String, String>> getModes()
    {
        return CollectionUtils.map(ProjectDependencyGraphBuilder.TransitiveMode.values(), new Mapping<ProjectDependencyGraphBuilder.TransitiveMode, Pair<String, String>>()
        {
            public Pair<String, String> map(ProjectDependencyGraphBuilder.TransitiveMode mode)
            {
                return CollectionUtils.asPair(mode.name(), I18N.format(mode.name() + ".label"));
            }
        });
    }
    
    public String execute()
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

        transitiveMode = mode.name();

        Project project = getRequiredProject();
        ProjectDependencyGraph dependencyGraph = projectDependencyGraphBuilder.build(project, mode);
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
