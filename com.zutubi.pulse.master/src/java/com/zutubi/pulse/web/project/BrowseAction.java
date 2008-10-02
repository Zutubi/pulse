package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectGroup;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.tove.config.user.BrowseViewConfiguration;
import com.zutubi.tove.config.ConfigurationRegistry;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateHierarchy;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.util.Sort;

import java.util.*;

/**
 */
public class BrowseAction extends ProjectActionSupport
{
    private List<ProjectsModel> models = new LinkedList<ProjectsModel>();
    private List<Project> invalidProjects = new LinkedList<Project>();
    private BrowseViewConfiguration browseConfig = new BrowseViewConfiguration();

    private ConfigurationTemplateManager configurationTemplateManager;

    public List<ProjectsModel> getModels()
    {
        return models;
    }

    public List<Project> getInvalidProjects()
    {
        return invalidProjects;
    }

    public BrowseViewConfiguration getBrowseConfig()
    {
        return browseConfig;
    }

    public ResultState getStateInProgress()
    {
        return ResultState.IN_PROGRESS;
    }

    public String execute() throws Exception
    {
        User user = getLoggedInUser();
        if(user != null)
        {
            browseConfig = user.getPreferences().getBrowseView();
        }

        TemplateHierarchy hierarchy = configurationTemplateManager.getTemplateHierarchy(ConfigurationRegistry.PROJECTS_SCOPE);

        final Comparator<String> comp = new Sort.StringComparator();
        List<Project> projects = projectManager.getProjects(true);

        // Filter invalid projects into a separate list.
        for (Project project: projects)
        {
            if (!projectManager.isProjectValid(project))
            {
                invalidProjects.add(project);
            }
        }

        projects.removeAll(invalidProjects);
        Collections.sort(invalidProjects, new ProjectComparator(comp));

        if (browseConfig.isGroupsShown())
        {
            // Create a model for each group, and for the ungrouped projects.
            List<ProjectGroup> groups = new ArrayList<ProjectGroup>(projectManager.getAllProjectGroups());
            Collections.sort(groups, new Comparator<ProjectGroup>()
            {
                public int compare(ProjectGroup o1, ProjectGroup o2)
                {
                    return comp.compare(o1.getName(), o2.getName());
                }
            });

            for (ProjectGroup group : groups)
            {
                addModel(group.getName(), true, group.getProjects(), hierarchy);
                projects.removeAll(group.getProjects());
            }
        }

        if (projects.size() > 0)
        {
            Collections.sort(projects, new ProjectComparator(comp));

            // CIB-1550: Only label as ungrouped is there are some other
            // groups.
            addModel(models.size() > 0 ? "ungrouped projects" : "all projects", false, projects, hierarchy);
        }

        return SUCCESS;
    }

    private void addModel(String name, boolean labelled, Collection<Project> projects, TemplateHierarchy hierarchy)
    {
        ProjectsModel model = new ProjectsModel(name, labelled);
        if(browseConfig.isHierarchyShown())
        {
            // The group can display all concrete projects plus all of their
            // ancestors (which may overlap).  The ancestors may not define the
            // label, but are included to prevent "holes" in the hierarchy.
            Set<String> includedInGroup = new HashSet<String>();
            for(Project p: projects)
            {
                TemplateNode node = hierarchy.getNodeById(p.getName());
                while(node != null)
                {
                    includedInGroup.add(node.getId());
                    node = node.getParent();
                }
            }

            processLevel(model, model.getRoot(), Arrays.asList(hierarchy.getRoot()), 0, includedInGroup);
        }
        else
        {
            for(Project p: projects)
            {
                model.getRoot().addChild(new ConcreteProjectModel(model, p, getBuilds(p), browseConfig.getBuildsPerProject()));
            }
        }

        models.add(model);
    }

    private void processLevel(ProjectsModel group, TemplateProjectModel parentModel, List<TemplateNode> nodes, int depth, Set<String> includedInGroup)
    {
        for (TemplateNode node : nodes)
        {
            if (node.isConcrete() || depth >= browseConfig.getHiddenHierarchyLevels())
            {
                if (includedInGroup.contains(node.getId()))
                {
                    ProjectModel model;
                    String name = node.getId();
                    if (node.isConcrete())
                    {
                        Project project = projectManager.getProject(name, true);
                        List<BuildResult> builds = getBuilds(project);
                        model = new ConcreteProjectModel(group, project, builds, browseConfig.getBuildsPerProject());
                    }
                    else
                    {
                        TemplateProjectModel template = new TemplateProjectModel(group, name);
                        processLevel(group, template, node.getChildren(), depth + 1, includedInGroup);
                        model = template;
                    }

                    parentModel.addChild(model);
                }
            }
            else
            {
                processLevel(group, parentModel, node.getChildren(), depth + 1, includedInGroup);
            }
        }
    }

    private List<BuildResult> getBuilds(Project project)
    {
        // We need to retrieve at least 2 to determine the health when the
        // latest build is in progress (this assumes one in progress build
        // per project).
        return buildManager.getLatestBuildResultsForProject(project, Math.max(2, browseConfig.getBuildsPerProject()));
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    private static class ProjectComparator implements Comparator<Project>
    {
        private final Comparator<String> comp;

        public ProjectComparator(Comparator<String> comp)
        {
            this.comp = comp;
        }

        public int compare(Project o1, Project o2)
        {
            if (o1.getName() == null)
            {
                return -1;
            }

            if (o2.getName() == null)
            {
                return 1;
            }

            return comp.compare(o1.getName(), o2.getName());
        }
    }
}
