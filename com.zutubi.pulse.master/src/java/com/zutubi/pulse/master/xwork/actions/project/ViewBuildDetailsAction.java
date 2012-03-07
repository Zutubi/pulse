package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.dependency.ivy.*;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.master.agent.MasterLocationProvider;
import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.Sort;
import com.zutubi.util.WebUtils;
import com.zutubi.util.logging.Logger;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Action for rendering the build details tab.
 */
public class ViewBuildDetailsAction extends BuildStatusActionBase
{
    private static final Logger LOG = Logger.getLogger(ViewBuildDetailsAction.class);

    private List<StageDependencyDetails> dependencyDetails = new LinkedList<StageDependencyDetails>();
    private List<StoredArtifact> implicitArtifacts = new LinkedList<StoredArtifact>();
    
    private MasterLocationProvider masterLocationProvider;
    private ConfigurationProvider configurationProvider;

    public List<StageDependencyDetails> getDependencyDetails()
    {
        return dependencyDetails;
    }

    public List<StoredArtifact> getImplicitArtifacts()
    {
        return implicitArtifacts;
    }

    public boolean isDependencyDetailsPresent()
    {
        return CollectionUtils.contains(dependencyDetails, new Predicate<StageDependencyDetails>()
        {
            public boolean satisfied(StageDependencyDetails stageDependencyDetails)
            {
                return stageDependencyDetails.isReportAvailable() && stageDependencyDetails.getDependencies().size() > 0;
            }
        });
    }

    public String execute()
    {
        super.execute();

        BuildResult result = getRequiredBuildResult();
        if (result.completed())
        {
            loadDependencyDetails(result);
        }
        
        CommandResult commandResult = getCommandResult();
        if (commandResult != null)
        {
            List<StoredArtifact> artifacts = commandResult.getArtifacts();
            CollectionUtils.filter(artifacts, new Predicate<StoredArtifact>()
            {
                public boolean satisfied(StoredArtifact storedArtifact)
                {
                    return !storedArtifact.isExplicit();
                }
            }, implicitArtifacts);
            final Sort.StringComparator stringComparator = new Sort.StringComparator();
            Collections.sort(implicitArtifacts, new Comparator<StoredArtifact>()
            {
                public int compare(StoredArtifact o1, StoredArtifact o2)
                {
                    return stringComparator.compare(o1.getName(), o2.getName());
                }
            });
        }

        return SUCCESS;
    }

    private void loadDependencyDetails(BuildResult result)
    {
        File dataDir = configurationManager.getDataDirectory();

        for (RecipeResultNode recipe : result)
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
                    dependencyDetails.add(new StageDependencyDetails(report));
                }
                else
                {
                    // The dependency details are no longer available, most likely due to a cleanup.
                    dependencyDetails.add(new StageDependencyDetails());
                }
            }
            catch (Exception e)
            {
                LOG.warning(e);
            }
        }
    }

    public void setMasterLocationProvider(MasterLocationProvider masterLocationProvider)
    {
        this.masterLocationProvider = masterLocationProvider;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    /**
     * A wrapper around the ivy dependencies report that extracts the data
     * needed to render the dependencies table.
     */
    public class StageDependencyDetails
    {
        private IvyRetrievalReport report;

        private List<StageDependency> dependencies;

        public StageDependencyDetails() throws Exception
        {
            this(null);
        }

        public StageDependencyDetails(IvyRetrievalReport report) throws Exception
        {
            this.report = report;
            if (this.report != null)
            {
                String masterLocation = masterLocationProvider.getMasterUrl();
                final File repositoryRoot = configurationManager.getUserPaths().getRepositoryRoot();
                IvyConfiguration configuration = new IvyConfiguration(masterLocation + WebManager.REPOSITORY_PATH);
                Urls urls = new Urls(configurationProvider.get(GlobalConfiguration.class).getBaseUrl());

                dependencies = new LinkedList<StageDependency>();
                for (Artifact artifact : report.getRetrievedArtifacts())
                {
                    StageDependency stageDependency = new StageDependency();
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
                    boolean isDependencyAvailable = projectManager.getProjectConfig(dependencyProjectName, false) != null;
                    if (isDependencyAvailable)
                    {
                        stageDependency.setProjectUrl(urls.project(uriComponentEncode(dependencyProjectName)));
                    }

                    ModuleRevisionId encodedMrid = IvyEncoder.encode(mrid);
                    String ivyPath = configuration.getIvyPath(encodedMrid, encodedMrid.getRevision());
                    File ivyFile = new File(repositoryRoot, ivyPath);
                    if (ivyFile.exists())
                    {
                        IvyModuleDescriptor ivyDescriptor = IvyModuleDescriptor.newInstance(ivyFile, configuration);
                        stageDependency.setBuildName(String.valueOf(ivyDescriptor.getBuildNumber()));
                        if (isDependencyAvailable)
                        {
                            stageDependency.setBuildUrl(urls.build(uriComponentEncode(dependencyProjectName), String.valueOf(ivyDescriptor.getBuildNumber())));
                        }
                    }
                    else
                    {
                        stageDependency.setBuildName("unknown");
                    }
                }

                Collections.sort(dependencies, new Comparator<StageDependency>()
                {
                    private Comparator<String> comparator = new Sort.StringComparator();

                    public int compare(StageDependency dependencyA, StageDependency dependencyB)
                    {
                        int comparison = comparator.compare(dependencyA.getProjectName(), dependencyB.getProjectName());
                        if (comparison != 0)
                        {
                            return comparison;
                        }
                        comparison = comparator.compare(dependencyA.getStageName(), dependencyB.getStageName());
                        if (comparison != 0)
                        {
                            return comparison;
                        }
                        return comparator.compare(dependencyA.getArtifactName(), dependencyB.getArtifactName());
                    }
                });
            }
        }

        public List<StageDependency> getDependencies()
        {
            return dependencies;
        }

        public boolean isReportAvailable()
        {
            return report != null;
        }
    }

    /**
     * A value holder for a row in the dependencies table.
     */
    public class StageDependency
    {
        private String stageName;
        private String projectName;
        private String projectUrl;
        private String buildName;
        private String buildUrl;
        private String artifactName;
        private String artifactUrl;

        public String getProjectName()
        {
            return projectName;
        }

        public void setProjectName(String projectName)
        {
            this.projectName = projectName;
        }

        public String getProjectUrl()
        {
            return projectUrl;
        }

        public void setProjectUrl(String projectUrl)
        {
            this.projectUrl = projectUrl;
        }

        public String getBuildName()
        {
            return buildName;
        }

        public void setBuildName(String buildName)
        {
            this.buildName = buildName;
        }

        public String getBuildUrl()
        {
            return buildUrl;
        }

        public void setBuildUrl(String buildUrl)
        {
            this.buildUrl = buildUrl;
        }

        public String getArtifactName()
        {
            return artifactName;
        }

        public void setArtifactName(String artifactName)
        {
            this.artifactName = artifactName;
        }

        public String getArtifactUrl()
        {
            return artifactUrl;
        }

        public void setArtifactUrl(String artifactUrl)
        {
            this.artifactUrl = artifactUrl;
        }

        public String getStageName()
        {
            return stageName;
        }

        public void setStageName(String stageName)
        {
            this.stageName = stageName;
        }
    }

}