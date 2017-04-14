/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.model;

import com.google.common.base.Function;
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
import com.zutubi.util.WebUtils;
import com.zutubi.util.adt.DAGraph;
import com.zutubi.util.logging.Logger;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.springframework.security.access.AccessDeniedException;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
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
        String contextPath = configurationManager.getSystemConfig().getContextPath();

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
            String artifactUrl = PathUtils.getPath(contextPath, WebManager.REPOSITORY_PATH, WebUtils.uriPathEncode(repositoryPath));

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
        DAGraph.Node<BuildResult> root = new DAGraph.Node<BuildResult>(build);
        BuildGraph graph = new BuildGraph(root);
        addLinkedNodes(graph, root, finder);
        return graph;
    }

    private void addLinkedNodes(BuildGraph graph, DAGraph.Node<BuildResult> node, LinkedBuildIdFinder finder)
    {
        List<Long> linkedIds = finder.getLinkedBuilds(node.getData().getId());
        for (Long linkedId: linkedIds)
        {
            DAGraph.Node<BuildResult> linkedNode = graph.findNodeByBuildId(linkedId);
            if (linkedNode == null)
            {
                try
                {
                    BuildResult linkedBuild = buildManager.getBuildResult(linkedId);
                    linkedNode = new DAGraph.Node<BuildResult>(linkedBuild);
                    node.connectNode(linkedNode);
                    addLinkedNodes(graph, linkedNode, finder);
                }
                catch (AccessDeniedException e)
                {
                    // Ignore builds the user cannot access.
                }
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
            return newArrayList(transform(buildDependencyLinkDao.findAllUpstreamDependencies(id), new Function<BuildDependencyLink, Long>()
            {
                public Long apply(BuildDependencyLink link)
                {
                    return link.getUpstreamBuildId();
                }
            }));
        }
    }

    /**
     * Finds all build ids downstream of a given build id.
     */
    private class DownstreamBuildIdFinder implements LinkedBuildIdFinder
    {
        public List<Long> getLinkedBuilds(long id)
        {
            return newArrayList(transform(buildDependencyLinkDao.findAllDownstreamDependencies(id), new Function<BuildDependencyLink, Long>()
            {
                public Long apply(BuildDependencyLink link)
                {
                    return link.getDownstreamBuildId();
                }
            }));
        }
    }
}
