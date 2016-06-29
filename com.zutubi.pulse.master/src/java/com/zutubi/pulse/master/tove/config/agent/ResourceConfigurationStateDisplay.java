package com.zutubi.pulse.master.tove.config.agent;

import com.google.common.collect.Lists;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.InMemoryResourceRepository;
import com.zutubi.pulse.core.PulseScope;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ResourceRequirementConfigurationToRequirement;
import com.zutubi.pulse.master.tove.format.MessagesAware;
import com.zutubi.tove.config.NamedConfigurationComparator;
import com.zutubi.util.Sort;
import com.zutubi.util.adt.TreeNode;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Iterables.transform;

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
        //ResourceRequirementConfigurationToRequirement fn = new ResourceRequirementConfigurationToRequirement();
        List<ProjectConfiguration> configs = Lists.newArrayList(projectManager.getAllProjectConfigs(false));
        Collections.sort(configs, new NamedConfigurationComparator());
        for (ProjectConfiguration project: configs)
        {
            PulseScope projectVariables = new PulseScope();
            for (ResourcePropertyConfiguration property: project.getProperties().values())
            {
                projectVariables.add(property.asResourceProperty());
            }

            if (resourceRepository.satisfies(transform(project.getRequirements(), new ResourceRequirementConfigurationToRequirement(projectVariables))))
            {
                String stagePart;
                List<String> compatibleStages = getCompatibleStageNames(resourceRepository, project, projectVariables);
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

    private List<String> getCompatibleStageNames(InMemoryResourceRepository resourceRepository, ProjectConfiguration project, PulseScope projectVariables)
    {
        List<String> stageNames = new LinkedList<String>();
        for (BuildStageConfiguration stage: project.getStages().values())
        {
            PulseScope stageVariables = projectVariables.createChild();
            for (ResourcePropertyConfiguration property: stage.getProperties().values())
            {
                stageVariables.add(property.asResourceProperty());
            }
            
            ResourceRequirementConfigurationToRequirement fn = new ResourceRequirementConfigurationToRequirement(stageVariables);
            if (resourceRepository.satisfies(transform(stage.getRequirements(), fn)))
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
