package com.zutubi.pulse.master.dependency.ivy;

import com.zutubi.pulse.core.dependency.ivy.IvyConfiguration;
import com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptor;
import com.zutubi.pulse.core.dependency.ivy.IvyUtils;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import static com.zutubi.pulse.master.dependency.ivy.MasterIvyModuleRevisionId.*;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependenciesConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.UnaryProcedure;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.MDArtifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
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
    public static final String NAMESPACE_EXTRA_ATTRIBUTES = "e";

    private IvyConfiguration configuration;

    public ModuleDescriptorFactory(IvyConfiguration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Create a module descriptor that contains the details necessary to retrieve the
     * dependencies as defined by the configuration.
     *
     * @param project   the configuration on which to base the descriptor
     * 
     * @return the created descriptor.
     *
     * @see #createRetrieveDescriptor(com.zutubi.pulse.master.tove.config.project.ProjectConfiguration, com.zutubi.pulse.master.model.BuildResult)
     */
    public DefaultModuleDescriptor createRetrieveDescriptor(ProjectConfiguration project)
    {
        return createRetrieveDescriptor(project, null);
    }
    
    /**
     * Create a module descriptor that contains the details necessary to retrieve the
     * dependencies as defined by the configuration.
     *
     * @param project   the configuration on which to base the descriptor
     * @param result    the build result for the current build
     *
     * @return the created descriptor.
     *
     * @see #createRetrieveDescriptor(com.zutubi.pulse.master.tove.config.project.ProjectConfiguration, com.zutubi.pulse.master.model.BuildResult)
     */
    public DefaultModuleDescriptor createRetrieveDescriptor(ProjectConfiguration project, BuildResult result)
    {
        return createRetrieveDescriptor(project, result, null);
    }

    /**
     * Create a module descriptor that contains the details necessary to retrieve the dependencies
     * as defined by the configuration.
     *
     * If a build result is specified, then the descriptors dependencies will match the results
     * dependencies when possible.  This is required when a build result is part of a larger build
     * and we want to ensure that dependencies are taken from that build rather than the configured
     * revision.
     *
     * The build result should be the build result associated with the build for which the retrieval
     * descriptor is used.
     *
     * The revision field defines the revision of the generated module descriptor.
     *
     * @param project   the configuration on which to base the descriptor.
     * @param result    the build result for the current build
     * @param revision  the revision of the descriptor
     * @return the created descriptor.
     */
    public DefaultModuleDescriptor createRetrieveDescriptor(ProjectConfiguration project, BuildResult result, String revision)
    {
        String status = (result != null && result.getStatus() != null) ? result.getStatus() : project.getDependencies().getStatus();
        IvyModuleDescriptor ivyDescriptor = new IvyModuleDescriptor(newInstance(project, revision), status, configuration);
        DependenciesConfiguration dependenciesConfiguration = project.getDependencies();
        for (DependencyConfiguration dependency : dependenciesConfiguration.getDependencies())
        {
            ModuleRevisionId dependencyMrid = getDependencyMRID(result, dependency);
            List<String> stageNames = new LinkedList<String>();
            if (!dependency.isAllStages())
            {
                stageNames = CollectionUtils.map(dependency.getStages(), new Mapping<BuildStageConfiguration, String>()
                {
                    public String map(BuildStageConfiguration stage)
                    {
                        return IvyUtils.ivyEncodeStageName(stage.getName());
                    }
                });
            }
            ivyDescriptor.addDependency(dependencyMrid, dependency.isTransitive(), stageNames.toArray(new String[stageNames.size()]));
        }

        return ivyDescriptor.getDescriptor();
    }

    /**
     * Determine the module revision that this dependency references.  If the build result is part of a
     * larger meta build that also built the dependency, then the dependency will reference the specific
     * dependency revision that was produced as part of the meta build.  Otherwise, the dependency will
     * reference the revision as defined by the configuration.
     *
     * @param result        a build result, or null if this processing occurs outside the context of a build.
     * @param dependency    the dependency configuration
     * 
     * @return  the module revision we should be depending on.
     */
    private ModuleRevisionId getDependencyMRID(BuildResult result, DependencyConfiguration dependency)
    {
        //NOTE: an alternate implementation might involve using the over arching build id to identify the
        //      set of results associated with it, and using this and the project name to identify the result
        //      of interest.

        ProjectConfiguration dependsOnProject = dependency.getProject();
        BuildResult dependsOnResult = (result != null) ? result.getDependsOn(dependsOnProject.getName()) : null;
        ModuleRevisionId dependencyMrid;
        if (dependsOnResult != null)
        {
            dependencyMrid = newInstance(dependsOnProject, dependsOnResult.getVersion());
        }
        else
        {
            dependencyMrid = newInstance(dependency);
        }
        return dependencyMrid;
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
        final DefaultModuleDescriptor descriptor = createRetrieveDescriptor(project, result, revision);

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
}
