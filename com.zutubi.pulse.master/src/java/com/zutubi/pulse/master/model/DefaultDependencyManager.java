package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.dependency.ivy.*;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.master.agent.MasterLocationProvider;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.Pair;
import com.zutubi.util.WebUtils;
import com.zutubi.util.logging.Logger;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.io.File;
import java.util.*;

import static com.zutubi.util.CollectionUtils.asPair;
import static com.zutubi.util.WebUtils.uriComponentEncode;

/**
 * Default implementation of DependencyManager.
 */
public class DefaultDependencyManager implements DependencyManager
{
    private static final Logger LOG = Logger.getLogger(DefaultDependencyManager.class);
    
    private BuildResultDao buildResultDao;
    private MasterConfigurationManager configurationManager;
    private ConfigurationProvider  configurationProvider;
    private MasterLocationProvider masterLocationProvider;
    private ProjectManager projectManager;

    public void addDependencyLinks(BuildResult build)
    {
        List<StageRetrievedArtifacts> stageDependencyDetails = loadRetrievedArtifacts(build);
        Set<Pair<Long, Long>> upstreamBuilds = new HashSet<Pair<Long, Long>>();
        for (StageRetrievedArtifacts details: stageDependencyDetails)
        {
            if (details.isArtifactInformationAvailable())
            {
                for (RetrievedArtifactSource dependency: details.getRetrievedArtifacts())
                {
                    Project project = projectManager.getProject(dependency.getProjectName(), true);
                    if (project != null && dependency.hasBuildNumber())
                    {
                        upstreamBuilds.add(asPair(project.getId(), dependency.getBuildNumber()));
                    }
                }
            }
        }

        for (Pair<Long, Long> upstream: upstreamBuilds)
        {
            BuildResult upstreamBuild = buildResultDao.findByProjectAndNumber(upstream.first, upstream.second);
            if (upstreamBuild != null)
            {
                BuildDependencyLink link = new BuildDependencyLink(upstreamBuild.getId(), build.getId());
                buildResultDao.save(link);
            }
        }
    }

    public List<StageRetrievedArtifacts> loadRetrievedArtifacts(BuildResult build)
    {
        assert build.completed();

        List<StageRetrievedArtifacts> dependencyDetails = new LinkedList<StageRetrievedArtifacts>();
        File dataDir = configurationManager.getDataDirectory();

        for (RecipeResultNode recipe : build)
        {
            CommandResult command = recipe.getResult().getCommandResult(RetrieveDependenciesCommand.COMMAND_NAME);
            if (command == null)
            {
                // no artifacts were retrieved for this command.
                continue;
            }

            String artifactPath = RetrieveDependenciesCommand.OUTPUT_NAME + "/"+ RetrieveDependenciesCommand.IVY_REPORT_FILE;
            File outputDir = new File(dataDir, command.getOutputDir());
            File reportFile = new File(outputDir, artifactPath);
            try
            {
                if (reportFile.isFile())
                {
                    IvyRetrievalReport report = IvyRetrievalReport.fromXml(reportFile);
                    dependencyDetails.add(new StageRetrievedArtifacts(recipe.getStageName(), processRetrievalReport(report)));
                }
                else
                {
                    // The dependency details are no longer available, most likely due to a cleanup.
                    dependencyDetails.add(new StageRetrievedArtifacts(recipe.getStageName(), null));
                }
            }
            catch (Exception e)
            {
                LOG.warning(e);
            }
        }

        return dependencyDetails;
    }

    private List<RetrievedArtifactSource> processRetrievalReport(IvyRetrievalReport report) throws Exception
    {
        String masterLocation = masterLocationProvider.getMasterUrl();
        final File repositoryRoot = configurationManager.getUserPaths().getRepositoryRoot();
        IvyConfiguration configuration = new IvyConfiguration(masterLocation + WebManager.REPOSITORY_PATH);
        Urls urls = new Urls(configurationProvider.get(GlobalConfiguration.class).getBaseUrl());

        List<RetrievedArtifactSource> dependencies = new LinkedList<RetrievedArtifactSource>();
        for (Artifact artifact : report.getRetrievedArtifacts())
        {
            RetrievedArtifactSource stageDependency = new RetrievedArtifactSource();
            dependencies.add(stageDependency);

            stageDependency.setArtifactName(artifact.getName() + "." + artifact.getExt());
            stageDependency.setStageName(artifact.getExtraAttribute("stage"));
            String repositoryPath = configuration.getArtifactPath(artifact);
            String artifactUrl = PathUtils.getPath(masterLocation, WebManager.REPOSITORY_PATH, WebUtils.uriPathEncode(repositoryPath));

            File artifactFile = new File(configurationManager.getUserPaths().getRepositoryRoot(), repositoryPath);
            if (artifactFile.isFile())
            {
                stageDependency.setArtifactUrl(artifactUrl);
            }

            ModuleRevisionId mrid = artifact.getModuleRevisionId();

            String dependencyProjectName = mrid.getName();
            stageDependency.setProjectName(dependencyProjectName);
            boolean isUpstreamProjectAvailable = projectManager.getProjectConfig(dependencyProjectName, false) != null;
            if (isUpstreamProjectAvailable)
            {
                stageDependency.setProjectUrl(urls.project(uriComponentEncode(dependencyProjectName)));
            }

            ModuleRevisionId encodedMrid = IvyEncoder.encode(mrid);
            String ivyPath = configuration.getIvyPath(encodedMrid, encodedMrid.getRevision());
            File ivyFile = new File(repositoryRoot, ivyPath);
            if (ivyFile.exists())
            {
                IvyModuleDescriptor ivyDescriptor = IvyModuleDescriptor.newInstance(ivyFile, configuration);
                stageDependency.setBuildNumber(ivyDescriptor.getBuildNumber());
                if (isUpstreamProjectAvailable)
                {
                    stageDependency.setBuildUrl(urls.build(uriComponentEncode(dependencyProjectName), String.valueOf(ivyDescriptor.getBuildNumber())));
                }
            }
        }

        Collections.sort(dependencies);

        return dependencies;
    }

    public void setBuildResultDao(BuildResultDao buildResultDao)
    {
        this.buildResultDao = buildResultDao;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setMasterLocationProvider(MasterLocationProvider masterLocationProvider)
    {
        this.masterLocationProvider = masterLocationProvider;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

}
