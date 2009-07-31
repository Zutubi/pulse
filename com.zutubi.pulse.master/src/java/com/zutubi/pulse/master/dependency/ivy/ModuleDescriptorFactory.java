package com.zutubi.pulse.master.dependency.ivy;

import com.zutubi.pulse.core.dependency.ivy.IvyClient;
import com.zutubi.pulse.core.dependency.ivy.IvyUtils;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.master.dependency.ivy.MasterIvyModuleRevisionId.EXTRA_ATTRIBUTE_STAGE;
import static com.zutubi.pulse.master.dependency.ivy.MasterIvyModuleRevisionId.EXTRA_ATTRIBUTE_SOURCE_FILE;
import static com.zutubi.pulse.master.dependency.ivy.MasterIvyModuleRevisionId.newInstance;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependenciesConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
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
import java.io.File;
import java.io.IOException;

/**
 * This factory creates an ivy module descriptor from the configuration contained by the
 * project hierarchy.
 */
public class ModuleDescriptorFactory
{
    public static final String NAMESPACE_EXTRA_ATTRIBUTES = "e";
    private static final String DUMMY_NAME = "configname";

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
     * @param project               the configuration on which to base the descriptor.
     * @param result                the build result that contains the artifact details.
     * @param revision              the revision of the descriptor
     * @param configurationManager  the system configuration manager
     * @return  the created descriptor.
     * @throws IOException is there is a problem creating the module descriptor.
     */
    public DefaultModuleDescriptor createDescriptor(ProjectConfiguration project, BuildResult result, String revision, final MasterConfigurationManager configurationManager) throws IOException
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

        final boolean[] success = { true };
        result.getRoot().forEachNode(new UnaryProcedure<RecipeResultNode>()
        {
            public void process(RecipeResultNode node)
            {
                if (node.getStageHandle() == 0)
                {
                    // skip the root.
                    return;
                }
                if (!success[0])
                {
                    // we have encountered a problem with one of the previous
                    // RecipeResultNodes so no point processing this one.
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
                                File outputDir = commandResult.getAbsoluteOutputDir(configurationManager.getDataDirectory());
                                File artifactFile = new File(outputDir, file.getPath());

                                String artifactFilename = PathUtils.getBaseName(file.getPath());

                                if (!addArtifact(stage, confName, result, artifact, p, artifactFile, artifactFilename, descriptor))
                                {
                                    success[0] = false;
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        });

        if (!success[0])
        {
            throw new IOException("Failed to generate dependency descriptor.  See build warnings for details.");
        }
        return descriptor;
    }

    private boolean addArtifact(String stage, String confName, RecipeResult result, StoredArtifact artifact, Pattern p, File artifactFile, String artifactFilename, DefaultModuleDescriptor descriptor)
    {
        Matcher m = p.matcher(artifactFilename);
        if (m.matches())
        {
            if (m.groupCount() < 2)
            {
                result.warning("Artifact pattern " + artifact.getArtifactPattern() + " failed to match the 2 expected groups in file " + artifactFile.getName() + ". Skipping.");
                return false;
            }
            String artifactName = m.group(1);
            if (artifactName == null || artifactName.trim().length() == 0)
            {
                result.warning("Artifact pattern " + artifact.getArtifactPattern() + " failed to capture an artifact name from file " + artifactFile.getName() + ". Skipping.");
                return false;
            }
            if (!IvyUtils.isValidArtifactName(artifactName))
            {
                result.warning("Artifact name '" + artifactName + "' contains one or more illegal characters. Only characters valid in a URI are allowed.");
                return false;
            }
            String artifactExt = m.group(2);
            if (artifactExt == null || artifactExt.trim().length() == 0)
            {
                result.warning("Artifact pattern " + artifact.getArtifactPattern() + " failed to capture an artifact name from file " + artifactFile.getName() + ". Skipping.");
                return false;
            }

            Map<String, String> extraAttributes = new HashMap<String, String>();
            extraAttributes.put(EXTRA_ATTRIBUTE_STAGE, IvyUtils.ivyEncodeStageName(stage));
            try
            {
                extraAttributes.put(EXTRA_ATTRIBUTE_SOURCE_FILE, artifactFile.getCanonicalPath());
            }
            catch (IOException e)
            {
                result.warning("Failed to get canonical path for artifact file.  Cause: " + e.getMessage());
                return false;
            }
            MDArtifact ivyArtifact = new MDArtifact(descriptor, artifactName, artifactExt, artifactExt, null, extraAttributes);
            ivyArtifact.addConfiguration(confName);
            descriptor.addArtifact(confName, ivyArtifact);
            return true;
        }
        else
        {
            result.warning("Artifact pattern " + artifact.getArtifactPattern() + " does not match artifact file " + artifactFile.getName() + ". Skipping.");
            return false;
        }
    }

    /**
     * Create a descriptor suitable for publishing a single file to the artifact repository.
     *
     * @param mrid                  the module revision id identifying the module to which the file will be published
     * @param stage                 the name of the stage that generated this artifact
     * @param artifactName          the name of the published artifact
     * @param artifactExtension     the type of the published artifact
     *
     * @return a descriptor suitable for publishing a file to the artifact repository.
     */
    public DefaultModuleDescriptor createPublishDescriptor(ModuleRevisionId mrid, String stage, String artifactName, String artifactExtension, File sourceFile) throws IOException
    {
        DefaultModuleDescriptor descriptor = new DefaultModuleDescriptor(mrid, "integration", null);
        descriptor.addConfiguration(new Configuration(IvyClient.CONFIGURATION_BUILD));
        descriptor.addExtraAttributeNamespace(NAMESPACE_EXTRA_ATTRIBUTES, "http://ant.apache.org/ivy/extra");

        descriptor.addConfiguration(new Configuration(DUMMY_NAME));

        Map<String, String> extraAttributes = new HashMap<String, String>();
        extraAttributes.put(NAMESPACE_EXTRA_ATTRIBUTES + ":stage", IvyUtils.ivyEncodeStageName(stage));
        extraAttributes.put(NAMESPACE_EXTRA_ATTRIBUTES + ":sourcefile", sourceFile.getCanonicalPath());
        MDArtifact ivyArtifact = new MDArtifact(descriptor, artifactName, artifactExtension, artifactExtension, null, extraAttributes);
        ivyArtifact.addConfiguration(DUMMY_NAME);
        descriptor.addArtifact(DUMMY_NAME, ivyArtifact);

        return descriptor;
    }
}
