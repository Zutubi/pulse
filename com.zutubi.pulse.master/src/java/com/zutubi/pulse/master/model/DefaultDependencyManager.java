package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.dependency.RepositoryAttributes;
import com.zutubi.pulse.core.dependency.ivy.*;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.master.agent.MasterLocationProvider;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.master.model.persistence.BuildDependencyLinkDao;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.WebUtils;
import com.zutubi.util.logging.Logger;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.zutubi.util.WebUtils.uriComponentEncode;

/**
 * Default implementation of DependencyManager.
 */
public class DefaultDependencyManager implements DependencyManager
{
    private static final Logger LOG = Logger.getLogger(DefaultDependencyManager.class);
    
    private File repositoryRoot;
    
    private BuildDependencyLinkDao buildDependencyLinkDao;
    private BuildManager buildManager;
    private MasterConfigurationManager configurationManager;
    private ConfigurationProvider  configurationProvider;
    private MasterLocationProvider masterLocationProvider;
    private ProjectManager projectManager;
    private RepositoryAttributes repositoryAttributes;

    public void init()
    {
        repositoryRoot = configurationManager.getUserPaths().getRepositoryRoot();
    }
    
    public void addDependencyLinks(BuildResult build, IvyModuleDescriptor ivyModuleDescriptor)
    {
        IvyConfiguration configuration = new IvyConfiguration(repositoryRoot.toURI().toString());
        DependencyDescriptor[] dependencies = ivyModuleDescriptor.getDescriptor().getDependencies();
        for (DependencyDescriptor dependency: dependencies)
        {
            ModuleRevisionId mrid = dependency.getDependencyRevisionId();
            String ivyPath = configuration.getIvyPath(mrid);
            File ivyFile = new File(repositoryRoot, ivyPath);
            if (ivyFile.isFile())
            {
                try
                {
                    IvyModuleDescriptor upstreamDescriptor = IvyModuleDescriptor.newInstance(ivyFile, configuration);
                    String upstreamModulePath = configuration.getModulePath(upstreamDescriptor.getModuleRevisionId());
                    String attribute = repositoryAttributes.getAttribute(upstreamModulePath, RepositoryAttributes.PROJECT_HANDLE);
                    if (attribute != null)
                    {
                        long projectHandle = Long.parseLong(attribute);
                        ProjectConfiguration projectConfig = configurationProvider.get(projectHandle, ProjectConfiguration.class);
                        Project project = projectManager.getProject(projectConfig.getProjectId(), true);
                        BuildResult upstreamBuild = buildManager.getByProjectAndNumber(project, upstreamDescriptor.getBuildNumber());
                        BuildDependencyLink link = new BuildDependencyLink(upstreamBuild.getId(), build.getId());
                        buildDependencyLinkDao.save(link);
                    }
                }
                catch (Exception e)
                {
                    LOG.warning(e);
                }
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
        String masterLocation = masterLocationProvider.getMasterLocation();
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

    public BuildGraph getUpstreamDependencyGraph(BuildResult build)
    {
        return getDependencyGraph(build, new UpstreamBuildIdFinder());
    }

    public BuildGraph getDownstreamDependencyGraph(BuildResult build)
    {
        return getDependencyGraph(build, new DownstreamBuildIdFinder());
    }

    public BuildGraph getDependencyGraph(BuildResult build, LinkedBuildIdFinder finder)
    {
        BuildGraph.Node root = new BuildGraph.Node(build);
        BuildGraph graph = new BuildGraph(root);
        addLinkedNodes(graph, root, finder);
        return graph;
    }

    private void addLinkedNodes(BuildGraph graph, BuildGraph.Node node, LinkedBuildIdFinder finder)
    {
        List<Long> linkedIds = finder.getLinkedBuilds(node.getBuild().getId());
        for (Long linkedId: linkedIds)
        {
            BuildGraph.Node linkedNode = graph.findNodeByBuildId(linkedId);
            if (linkedNode == null)
            {
                BuildResult linkedBuild = buildManager.getBuildResult(linkedId);
                linkedNode = new BuildGraph.Node(linkedBuild);
                node.connectNode(linkedNode);
                addLinkedNodes(graph, linkedNode, finder);
            }
            else
            {
                node.connectNode(linkedNode);
            }
        }
    }

    public void setBuildDependencyLinkDao(BuildDependencyLinkDao buildDependencyLinkDao)
    {
        this.buildDependencyLinkDao = buildDependencyLinkDao;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
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

    public void setRepositoryAttributes(RepositoryAttributes repositoryAttributes)
    {
        this.repositoryAttributes = repositoryAttributes;
    }

    /**
     * Abstraction over the lookup of builds linked via dependencies, so algorithms can be written independently of the
     * dependency direction.
     */
    private interface LinkedBuildIdFinder
    {
        List<Long> getLinkedBuilds(long id);
    }

    /**
     * Finds all build ids upstream of a given build id.
     */
    private class UpstreamBuildIdFinder implements LinkedBuildIdFinder
    {
        public List<Long> getLinkedBuilds(long id)
        {
            return CollectionUtils.map(buildDependencyLinkDao.findAllUpstreamDependencies(id), new Mapping<BuildDependencyLink, Long>()
            {
                public Long map(BuildDependencyLink link)
                {
                    return link.getUpstreamBuildId();
                }
            });
        }
    }

    /**
     * Finds all build ids downstream of a given build id.
     */
    private class DownstreamBuildIdFinder implements LinkedBuildIdFinder
    {
        public List<Long> getLinkedBuilds(long id)
        {
            return CollectionUtils.map(buildDependencyLinkDao.findAllDownstreamDependencies(id), new Mapping<BuildDependencyLink, Long>()
            {
                public Long map(BuildDependencyLink link)
                {
                    return link.getDownstreamBuildId();
                }
            });
        }
    }
}
