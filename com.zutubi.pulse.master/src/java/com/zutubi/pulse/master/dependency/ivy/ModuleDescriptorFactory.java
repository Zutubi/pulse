package com.zutubi.pulse.master.dependency.ivy;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.dependency.ivy.IvyConfiguration;
import com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptor;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependenciesConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.UnaryProcedure;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This factory creates an ivy module descriptor from the pulse data and configuration.
 */
public class ModuleDescriptorFactory
{
    private static final Messages I18N = Messages.getInstance(ModuleDescriptorFactory.class);
    private static final Mapping<BuildStageConfiguration, String> STAGE_NAME_MAPPING = new StageNameMapping();
    private final IvyConfiguration configuration;
    private final MasterConfigurationManager configurationManager;

    public ModuleDescriptorFactory(IvyConfiguration configuration, MasterConfigurationManager configurationManager)
    {
        this.configuration = configuration;
        this.configurationManager = configurationManager;
    }

    /**
     * Create a module descriptor that contains the details necessary to retrieve the
     * dependencies as defined by the configuration.
     *
     * @param project the configuration on which to base the descriptor
     * @return the created descriptor.
     * @see #createRetrieveDescriptor(ProjectConfiguration, BuildResult)
     */
    public IvyModuleDescriptor createRetrieveDescriptor(ProjectConfiguration project)
    {
        return createRetrieveDescriptor(project, null);
    }

    /**
     * Create a module descriptor that contains the details necessary to retrieve the
     * dependencies as defined by the configuration.
     * <p/>
     * When the specified build result indicates that one of the projects dependencies
     * was rebuilt as part of the build, then the version of the dependency produced by
     * the recent build is used if possible.  This ensures that consistent artifacts
     * are used across an extended build.
     *
     * @param project the configuration on which to base the descriptor
     * @param result  the build result for the current build
     * @return the created descriptor.
     * @see #createRetrieveDescriptor(ProjectConfiguration, BuildResult)
     */
    public IvyModuleDescriptor createRetrieveDescriptor(ProjectConfiguration project, BuildResult result)
    {
        return createRetrieveDescriptor(project, result, null);
    }

    /**
     * Create a module descriptor that contains the details necessary to retrieve the dependencies
     * as defined by the configuration.
     * <p/>
     * When the specified build result indicates that one of the projects dependencies
     * was rebuilt as part of the build, then the version of the dependency produced by
     * the recent build is used if possible.  This ensures that consistent artifacts
     * are used across an extended build.
     * <p/>
     * The revision field defines the revision of the generated module descriptor.
     *
     * @param project  the configuration on which to base the descriptor.
     * @param result   the build result for the current build
     * @param revision the revision of the descriptor
     * @return the created descriptor.
     */
    public IvyModuleDescriptor createRetrieveDescriptor(ProjectConfiguration project, BuildResult result, String revision)
    {
        IvyModuleDescriptor ivyDescriptor = newDescriptor(project, result, revision);

        addDependencies(project, result, ivyDescriptor);

        return ivyDescriptor;
    }

    /**
     * Create a descriptor based on the specified configuration and the build result.  This is a
     * complete descriptor that contains publish and retrieval information.
     *
     * @param project  the configuration on which to base the descriptor.
     * @param result   the build result that contains the artifact details.
     * @param revision the revision of the descriptor
     * @return the created descriptor.
     * @throws IOException is there is a problem creating the module descriptor.
     */
    public IvyModuleDescriptor createDescriptor(ProjectConfiguration project, BuildResult result, String revision) throws IOException
    {
        IvyModuleDescriptor ivyDescriptor = newDescriptor(project, result, revision);

        addDependencies(project, result, ivyDescriptor);
        addArtifacts(result, ivyDescriptor);

        return ivyDescriptor;
    }

    private void addDependencies(ProjectConfiguration project, BuildResult result, IvyModuleDescriptor ivyDescriptor)
    {
        DependenciesConfiguration dependencies = project.getDependencies();
        for (DependencyConfiguration dependency : dependencies.getDependencies())
        {
            ModuleRevisionId dependencyMrid = getDependencyMRID(result, dependency);
            List<String> stageNames = new LinkedList<String>();
            switch (dependency.getStageType())
            {
                case ALL_STAGES:
                    stageNames.add(IvyModuleDescriptor.ALL_STAGES);
                    break;
                case CORRESPONDING_STAGES:
                    stageNames.add(IvyModuleDescriptor.CORRESPONDING_STAGE);
                    ivyDescriptor.addOptionalDependency(dependencyMrid.getName());
                    break;
                case SELECTED_STAGES:
                    CollectionUtils.map(dependency.getStages(), STAGE_NAME_MAPPING, stageNames);
                    break;
            }

            for (BuildStageConfiguration stage: project.getStages().values())
            {
                ivyDescriptor.addDependency(dependencyMrid, stage.getName(), dependency.isTransitive(), stageNames.toArray(new String[stageNames.size()]));
            }
        }
    }

    private IvyModuleDescriptor newDescriptor(ProjectConfiguration project, BuildResult result, String revision)
    {
        String status = result != null && result.getStatus() != null ? result.getStatus() : project.getDependencies().getStatus();
        return new IvyModuleDescriptor(MasterIvyModuleRevisionId.newInstance(project, revision), status, configuration);
    }

    /**
     * Determine the module revision that this dependency references.  If the build result is part of a
     * larger meta build that also built the dependency, then the dependency will reference the specific
     * dependency revision that was produced as part of the meta build.  Otherwise, the dependency will
     * reference the revision as defined by the configuration.
     *
     * @param result     a build result, or null if this processing occurs outside the context of a build.
     * @param dependency the dependency configuration
     * @return the module revision we should be depending on.
     */
    private ModuleRevisionId getDependencyMRID(BuildResult result, DependencyConfiguration dependency)
    {
        ProjectConfiguration dependsOnProject = dependency.getProject();
        BuildResult dependsOnResult = result != null ? result.getDependsOn(dependsOnProject.getName()) : null;
        ModuleRevisionId dependencyMrid;
        if (dependsOnResult != null)
        {
            dependencyMrid = MasterIvyModuleRevisionId.newInstance(dependsOnProject, dependsOnResult.getVersion());
        }
        else
        {
            dependencyMrid = MasterIvyModuleRevisionId.newInstance(dependency);
        }
        return dependencyMrid;
    }

    private void addArtifacts(BuildResult result, IvyModuleDescriptor ivyDescriptor)
    {
        final Collection<ArtifactDetail> artifacts = new LinkedList<ArtifactDetail>();

        result.getRoot().forEachNode(new UnaryProcedure<RecipeResultNode>()
        {
            public void run(RecipeResultNode node)
            {
                if (node.getStageHandle() == 0) // skip the root.
                {
                    return;
                }

                RecipeResult recipeResult = node.getResult();
                for (CommandResult commandResult : recipeResult.getCommandResults())
                {
                    for (StoredArtifact storedArtifact : commandResult.getArtifacts())
                    {
                        if (storedArtifact.isPublish())
                        {
                            for (StoredFileArtifact storedFileArtifact : storedArtifact.getChildren())
                            {
                                artifacts.add(new ArtifactDetail(node, recipeResult, commandResult, storedArtifact, storedFileArtifact, configurationManager.getDataDirectory()));
                            }
                        }
                    }
                }
            }
        });

        boolean successful = true;
        for (ArtifactDetail artifact : artifacts)
        {
            if (!addArtifact(artifact, ivyDescriptor))
            {
                successful = false;
            }
        }
        if (!successful)
        {
            result.warning(I18N.format("artifact.failure"));
        }
    }

    private boolean addArtifact(ArtifactDetail artifactDetail, IvyModuleDescriptor descriptor)
    {
        RecipeResult result = artifactDetail.getRecipeResult();
        String artifactFilename = artifactDetail.getArtifactFile().getName();

        Matcher m = artifactDetail.getPattern().matcher(artifactFilename);
        if (m.matches())
        {
            String artifactName = IvyModuleDescriptor.UNKNOWN;
            if (m.groupCount() > 0)
            {
                artifactName = m.group(1);
            }
            String artifactExt = IvyModuleDescriptor.UNKNOWN;
            if (m.groupCount() > 1)
            {
                artifactExt = m.group(2);
            }

            if (artifactName == null || artifactName.trim().length() == 0)
            {
                result.warning(I18N.format("pattern.match.missingArtifactName", artifactDetail.getArtifactPattern(), artifactFilename));
                return false;
            }

            descriptor.addArtifact(artifactName, artifactExt, artifactExt, artifactDetail.getArtifactFile(), artifactDetail.getStageName());
            return true;
        }
        else
        {
            result.warning(I18N.format("pattern.match.failed", artifactDetail.getArtifactPattern(), artifactFilename));
            return false;
        }
    }

    /**
     * A value class used to hold context details about an artifact.  This artifact detail
     * represents a single stored artifact file.
     */
    private static class ArtifactDetail
    {
        private final RecipeResultNode node;
        private final RecipeResult recipeResult;
        private final CommandResult commandResult;
        private final StoredArtifact storedArtifact;
        private final StoredFileArtifact storedArtifactFile;
        private final File dataDir;

        private ArtifactDetail(RecipeResultNode node, RecipeResult recipeResult, CommandResult commandResult, StoredArtifact storedArtifact, StoredFileArtifact storedArtifactFile, File dataDir)
        {
            this.node = node;
            this.recipeResult = recipeResult;
            this.commandResult = commandResult;
            this.storedArtifact = storedArtifact;
            this.storedArtifactFile = storedArtifactFile;
            this.dataDir = dataDir;
        }

        public File getArtifactFile()
        {
            File outputDir = commandResult.getAbsoluteOutputDir(dataDir);
            return new File(outputDir, storedArtifactFile.getPath());
        }

        public Pattern getPattern()
        {
            return Pattern.compile(storedArtifact.getArtifactPattern());
        }

        public String getArtifactPattern()
        {
            return storedArtifact.getArtifactPattern();
        }

        public String getStageName()
        {
            return node.getStageName();
        }

        public RecipeResult getRecipeResult()
        {
            return recipeResult;
        }
    }

    private static class StageNameMapping implements Mapping<BuildStageConfiguration, String>
    {
        public String map(BuildStageConfiguration t)
        {
            return t.getName();
        }
    }
}
