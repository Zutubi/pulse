package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.InMemoryResourceRepository;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ResourceRequirementConfigurationToRequirement;
import com.zutubi.pulse.master.tove.format.MessagesAware;
import com.zutubi.tove.config.NamedConfigurationComparator;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Sort;
import com.zutubi.util.TreeNode;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Formats state information for resource requirements.
 */
public class ResourceConfigurationStateDisplay implements MessagesAware
{
    private ProjectManager projectManager;
    private Messages messages;

    /**
     * Formats information about the projects and stages that may run on an
     * agent with the given resources configured.  In general this is a two-
     * level tree with projects and stages nested under them.  However, where
     * simpler cases like all/none/one projects/stages are found, the structure
     * is simplified.
     *
     * @param resources resources configured on the agent
     * @return formatted state indicating which projects and stages could run
     *         on an agent with the given resources
     */
    public Object formatCollectionCompatibleStages(Collection<ResourceConfiguration> resources)
    {
        InMemoryResourceRepository resourceRepository = new InMemoryResourceRepository();
        resourceRepository.addAllResources(resources);

        TreeNode<String> result = new TreeNode<String>(null);
        boolean incompatibilityFound = false;
        ResourceRequirementConfigurationToRequirement fn = new ResourceRequirementConfigurationToRequirement();
        List<ProjectConfiguration> configs = new LinkedList<ProjectConfiguration>(projectManager.getAllProjectConfigs(false));
        Collections.sort(configs, new NamedConfigurationComparator());
        for (ProjectConfiguration project: configs)
        {
            List<ResourceRequirement> requirements = CollectionUtils.map(project.getRequirements(), fn);
            if (resourceRepository.satisfies(requirements))
            {
                String stagePart;
                List<String> compatibleStages = getCompatibleStageNames(resourceRepository, fn, project);
                int projectStageCount = project.getStages().size();
                int compatibleStageCount = compatibleStages.size();
                if (compatibleStageCount == projectStageCount)
                {
                    stagePart = messages.format("compatibleStages.all.stages");
                }
                else
                {
                    incompatibilityFound = true;
                    if (compatibleStageCount == 0)
                    {
                        stagePart = messages.format("compatibleStages.no.stages");
                    }
                    else
                    {
                        stagePart = messages.format("compatibleStages.stages", compatibleStageCount, projectStageCount);
                    }
                }

                TreeNode<String> projectNode = new TreeNode<String>(messages.format("compatibleStages.project", project.getName(), stagePart));
                result.add(projectNode);
                if (compatibleStageCount > 0 && compatibleStageCount < projectStageCount)
                {
                    Collections.sort(compatibleStages, new Sort.StringComparator());
                    for (String stage: compatibleStages)
                    {
                        projectNode.add(new TreeNode<String>(stage));
                    }
                }
            }
            else
            {
                incompatibilityFound = true;
            }
        }

        if (!incompatibilityFound)
        {
            return messages.format("compatibleStages.all");
        }
        else if (result.size() == 1)
        {
            return messages.format("compatibleStages.none");
        }
        else if (result.size() == 2)
        {
            return result.getChildren().get(0).getData();
        }
        else
        {
            return result;
        }
    }

    private List<String> getCompatibleStageNames(InMemoryResourceRepository resourceRepository, ResourceRequirementConfigurationToRequirement fn, ProjectConfiguration project)
    {
        List<String> stageNames = new LinkedList<String>();
        for (BuildStageConfiguration stage: project.getStages().values())
        {
            List<ResourceRequirement> requirements = CollectionUtils.map(stage.getRequirements(), fn);
            if (resourceRepository.satisfies(requirements))
            {
                stageNames.add(stage.getName());
            }
        }

        return stageNames;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setMessages(Messages messages)
    {
        this.messages = messages;
    }
}
