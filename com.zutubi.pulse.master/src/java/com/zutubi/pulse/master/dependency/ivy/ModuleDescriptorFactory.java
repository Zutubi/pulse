package com.zutubi.pulse.master.dependency.ivy;

import org.apache.ivy.core.module.descriptor.*;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import com.zutubi.pulse.master.tove.config.project.*;
import static com.zutubi.pulse.master.dependency.ivy.MasterIvyModuleRevisionId.*;
import com.zutubi.pulse.core.dependency.ivy.IvyClient;
import com.zutubi.pulse.core.dependency.ivy.IvyUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.StringUtils;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

/**
 * This factory creates an ivy module descriptor from the configuration contained by the
 * project hierarchy.
 */
public class ModuleDescriptorFactory
{
    public static final String NAMESPACE_EXTRA_ATTRIBUTES = "e";

    /**
     * Create a descriptor based on the specified configuration.
     * @param project   the configuration on which to base the descriptor
     * @return  the created descriptor.
     */
    public DefaultModuleDescriptor createDescriptor(ProjectConfiguration project)
    {
        return createDescriptor(project, null);
    }

    /**
     * Create a descriptor based on the specified configuration, setting the specified
     * revision on the descriptor.
     * @param project   the configuration on which to base the descriptor.
     * @param revision  the revision of the descriptor
     * @return  the created descriptor.
     */
    public DefaultModuleDescriptor createDescriptor(ProjectConfiguration project, String revision)
    {
        ModuleRevisionId mrid = newInstance(project, revision);

        DependenciesConfiguration dependenciesConfiguration = project.getDependencies();

        String status = project.getDependencies().getStatus();
        DefaultModuleDescriptor descriptor = new DefaultModuleDescriptor(mrid, status, null);
        descriptor.addConfiguration(new Configuration(IvyClient.CONFIGURATION_BUILD));
        descriptor.addExtraAttributeNamespace(NAMESPACE_EXTRA_ATTRIBUTES, "http://ant.apache.org/ivy/extra");

        for (DependencyConfiguration dependency : dependenciesConfiguration.getDependencies())
        {
            ModuleRevisionId dependencyMrid = newInstance(dependency);
            DefaultDependencyDescriptor depDesc = new DefaultDependencyDescriptor(descriptor, dependencyMrid, true, false, dependency.isTransitive());

            String stages = DependencyConfiguration.ALL_STAGES;
            if (!dependency.isAllStages())
            {
                List<String> stageNames = CollectionUtils.map(dependency.getStages(), new Mapping<BuildStageConfiguration, String>()
                {
                    public String map(BuildStageConfiguration stage)
                    {
                        return IvyUtils.ivyEncodeStageName(stage.getName());
                    }
                });
                stages = StringUtils.join(",", stageNames);
            }
            depDesc.addDependencyConfiguration(IvyClient.CONFIGURATION_BUILD, stages);

            descriptor.addDependency(depDesc);
        }

        for (BuildStageConfiguration stage : project.getStages().values())
        {
            List<PublicationConfiguration> publications = new LinkedList<PublicationConfiguration>();
            publications.addAll(project.getDependencies().getPublications());
            publications.addAll(stage.getPublications());
            if (publications.size() > 0)
            {
                String confName = IvyUtils.ivyEncodeStageName(stage.getName());
                descriptor.addConfiguration(new Configuration(confName));
                for (PublicationConfiguration artifact : publications)
                {
                    Map<String, String> extraAttributes = new HashMap<String, String>();
                    extraAttributes.put(EXTRA_ATTRIBUTE_STAGE, IvyUtils.ivyEncodeStageName(stage.getName()));
                    MDArtifact ivyArtifact = new MDArtifact(descriptor, artifact.getName(), artifact.getExt(), artifact.getExt(), null, extraAttributes);
                    ivyArtifact.addConfiguration(confName);
                    descriptor.addArtifact(confName, ivyArtifact);
                }
            }
        }
        return descriptor;
    }
}
