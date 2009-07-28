package com.zutubi.pulse.master.dependency.ivy;

import com.zutubi.pulse.core.dependency.ivy.IvyClient;
import com.zutubi.pulse.core.dependency.ivy.IvyUtils;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import static com.zutubi.pulse.master.dependency.ivy.MasterIvyModuleRevisionId.EXTRA_ATTRIBUTE_STAGE;
import static com.zutubi.pulse.master.dependency.ivy.MasterIvyModuleRevisionId.newInstance;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependenciesConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.StringUtils;
import com.zutubi.util.UnaryProcedure;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.MDArtifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This factory creates an ivy module descriptor from the configuration contained by the
 * project hierarchy.
 */
public class ModuleDescriptorFactory
{
    public static final String NAMESPACE_EXTRA_ATTRIBUTES = IvyClient.NAMESPACE_EXTRA_ATTRIBUTES;

    /**
     * Create a descriptor based on the specified configuration.
     *
     * @param project the configuration on which to base the descriptor
     * @return the created descriptor.
     */
    public DefaultModuleDescriptor createDescriptor(ProjectConfiguration project)
    {
        return createRetrieveDescriptor(project, null);
    }

    /**
     * Create an ivy descriptor based on the specified configuration, that can be used for
     * the dependency retrieval process.
     *
     * @param project  the configuration on which to base the descriptor.
     * @param revision the revision of the descriptor
     * @return the created descriptor.
     */
    public DefaultModuleDescriptor createRetrieveDescriptor(ProjectConfiguration project, String revision)
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

        return descriptor;
    }

    /**
     * Create a descriptor based on the specified configuration and the build result.  This is a
     * complete descriptor that contains publish and retrieval information.
     *
     * @param project   the configuration on which to base the descriptor.
     * @param result    the build result that contains the artifact details.
     * @param revision  the revision of the descriptor
     * @return  the created descriptor.
     */
    public DefaultModuleDescriptor createDescriptor(ProjectConfiguration project, BuildResult result, String revision)
    {
        ModuleRevisionId mrid = newInstance(project, revision);

        DependenciesConfiguration dependenciesConfiguration = project.getDependencies();

        String status = project.getDependencies().getStatus();
        final DefaultModuleDescriptor descriptor = new DefaultModuleDescriptor(mrid, status, null);
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

        result.getRoot().forEachNode(new UnaryProcedure<RecipeResultNode>()
        {
            public void process(RecipeResultNode node)
            {
                if (node.getStageHandle() == 0)
                {
                    // skip the root.
                    return;
                }

                String stage = node.getStageName();

                String confName = IvyUtils.ivyEncodeStageName(stage);
                descriptor.addConfiguration(new Configuration(confName));

                RecipeResult result = node.getResult();
                for (CommandResult commandResult : result.getCommandResults())
                {
                    for (StoredArtifact artifact : commandResult.getArtifacts())
                    {
                        Pattern p = Pattern.compile(artifact.getArtifactPattern());

                        if (artifact.isPublish())
                        {
                            for (StoredFileArtifact file : artifact.getChildren())
                            {
                                String artifactFilename = PathUtils.getBaseName(file.getPath());

                                Matcher m = p.matcher(artifactFilename);
                                if (m.matches())
                                {
                                    String artifactName = m.group(1);
                                    String artifactExt = m.group(2);

                                    Map<String, String> extraAttributes = new HashMap<String, String>();
                                    extraAttributes.put(EXTRA_ATTRIBUTE_STAGE, IvyUtils.ivyEncodeStageName(stage));
                                    MDArtifact ivyArtifact = new MDArtifact(descriptor, artifactName, artifactExt, artifactExt, null, extraAttributes);
                                    ivyArtifact.addConfiguration(confName);
                                    descriptor.addArtifact(confName, ivyArtifact);
                                }
                            }
                        }
                    }
                }
            }
        });

        return descriptor;
    }
}
