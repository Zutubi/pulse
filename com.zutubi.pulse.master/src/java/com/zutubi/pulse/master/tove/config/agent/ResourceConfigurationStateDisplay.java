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
import com.zutubi.tove.config.NamedConfigurationComparator;
import com.zutubi.tove.ui.format.MessagesAware;
import com.zutubi.util.Sort;

import java.util.*;

import static com.google.common.collect.Iterables.transform;

/**
 * Formats state information for resource requirements.
 */
public class ResourceConfigurationStateDisplay implements MessagesAware
{
    private ProjectManager projectManager;
    private Messages messages;

    /**
     * Formats information about the projects and stages that may run on an agent with the given
     * resources configured.  In general this is a map from projects to stage lists. However, where
     * simpler cases like all/none/one projects/stages are found, the structure is simplified.
     *
     * @param resources resources configured on the agent
     * @return formatted state indicating which projects and stages could run on an agent with the
     *         given resources.  Generally this is a map from project string to stage string list
     *         (which may be empty).  This is collapsed to a single string if possible to simplify
     *         display.  An example map may look like:
     *
     *         <ul>
     *             <li>project 1 (all stages) -> []</li>
     *             <li>project 2 (2 of 3 stages) -> [stage 1, stage 3]</li>
     *             <li>project 3 (no stages) -> []</li>
     *         </ul>
     *
     *         Note how stages are omitted when either all or none are compatible.
     */
    public Object formatCollectionCompatibleStages(Collection<ResourceConfiguration> resources)
    {
        InMemoryResourceRepository resourceRepository = new InMemoryResourceRepository();
        resourceRepository.addAllResources(resources);

        Map<String, List<String>> result = new LinkedHashMap<>();
        boolean incompatibilityFound = false;
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

                String projectKey = messages.format("compatibleStages.project", project.getName(), stagePart);
                List<String> stages = Collections.emptyList();
                if (compatibleStageCount > 0 && compatibleStageCount < projectStageCount)
                {
                    Collections.sort(compatibleStages, new Sort.StringComparator());
                    stages = compatibleStages;
                }

                result.put(projectKey, stages);
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
        else if (result.size() == 0)
        {
            return messages.format("compatibleStages.none");
        }
        else if (result.size() == 1)
        {
            Map.Entry<String, List<String>> projectEntry = result.entrySet().iterator().next();
            List<String> stages = projectEntry.getValue();
            if (stages.size() == 0)
            {
                return projectEntry.getKey();
            }
        }

        return result;
    }

    private List<String> getCompatibleStageNames(InMemoryResourceRepository resourceRepository, ProjectConfiguration project, PulseScope projectVariables)
    {
        List<String> stageNames = new ArrayList<>();
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
