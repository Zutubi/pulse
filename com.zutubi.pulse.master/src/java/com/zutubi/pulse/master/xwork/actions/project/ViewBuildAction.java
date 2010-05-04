package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.dependency.ivy.IvyConfiguration;
import com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptor;
import com.zutubi.pulse.core.dependency.ivy.IvyRetrievalReport;
import com.zutubi.pulse.core.dependency.ivy.RetrieveDependenciesCommand;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.master.agent.MasterLocationProvider;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.NamedConfigurationComparator;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.Sort;
import com.zutubi.util.WebUtils;
import com.zutubi.util.logging.Logger;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.io.File;
import java.util.*;

/**
 *
 *
 */
public class ViewBuildAction extends CommandActionBase
{
    public static final String FAILURE_LIMIT_PROPERTY = "pulse.test.failure.limit";
    public static final int DEFAULT_FAILURE_LIMIT = 100;

    private static final Logger LOG = Logger.getLogger(ViewBuildAction.class);

    private List<PersistentChangelist> changelists;
    private BuildColumns summaryColumns;
    /**
     * Insanity to work around lack of locals in velocity.
     */
    private Stack<String> pathStack = new Stack<String>();
    private String responsibleOwner;
    private String responsibleComment;
    private boolean canClearResponsible = false;
    private List<ActionLink> actions = new LinkedList<ActionLink>();
    private List<BuildHookConfiguration> hooks;

    private MasterConfigurationManager configurationManager;
    private SystemPaths systemPaths;

    private MasterLocationProvider masterLocationProvider;
    private ConfigurationProvider configurationProvider;
    private List<StageDependencyDetails> dependencyDetails = new LinkedList<StageDependencyDetails>();

    public boolean haveRecipeResultNode()
    {
        return getRecipeResultNode() != null;
    }

    public boolean haveCommandResult()
    {
        return getCommandResult() != null;
    }

    public BuildResult getResult()
    {
        return getBuildResult();
    }

    public BuildColumns getSummaryColumns()
    {
        // Lazy init: not always used.
        if(summaryColumns == null)
        {
            User u = getLoggedInUser();
            summaryColumns = new BuildColumns(u == null ? UserPreferencesConfiguration.defaultProjectColumns() : u.getPreferences().getProjectSummaryColumns(), accessManager);
        }
        return summaryColumns;
    }

    public Map<String, String> getCustomFields(Result result)
    {
        ResultCustomFields customFields = new ResultCustomFields(result.getAbsoluteOutputDir(configurationManager.getDataDirectory()));
        return customFields.load();
    }

    public int getFailureLimit()
    {
        int limit = DEFAULT_FAILURE_LIMIT;
        String property = System.getProperty(FAILURE_LIMIT_PROPERTY);
        if(property != null)
        {
            try
            {
                limit = Integer.parseInt(property);
            }
            catch(NumberFormatException e)
            {
                LOG.warning(e);
            }
        }

        return limit;
    }

    public String getResponsibleOwner()
    {
        return responsibleOwner;
    }

    public String getResponsibleComment()
    {
        return responsibleComment;
    }

    public boolean isCanClearResponsible()
    {
        return canClearResponsible;
    }

    public boolean canDeleteComment(final long id)
    {
        Comment comment = CollectionUtils.find(getBuildResult().getComments(), new EntityWithIdPredicate<Comment>(id));
        if (comment == null)
        {
            return false;
        }

        return accessManager.hasPermission(AccessManager.ACTION_DELETE, comment);
    }

    public List<ActionLink> getActions()
    {
        return actions;
    }

    public List<BuildHookConfiguration> getHooks()
    {
        return hooks;
    }

    public List<StageDependencyDetails> getDependencyDetails()
    {
        return dependencyDetails;
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
        final BuildResult result = getRequiredBuildResult();
        Project project = result.getProject();
        boolean canWrite = accessManager.hasPermission(AccessManager.ACTION_WRITE, project);
        if (canWrite)
        {
            ProjectConfiguration projectConfig = getRequiredProject().getConfig();
            hooks = CollectionUtils.filter(projectConfig.getBuildHooks().values(), new Predicate<BuildHookConfiguration>()
            {
                public boolean satisfied(BuildHookConfiguration hookConfiguration)
                {
                    return hookConfiguration.canManuallyTriggerFor(result);
                }
            });
            Collections.sort(hooks, new NamedConfigurationComparator());
        }
        else
        {
            hooks = Collections.emptyList();
        }

        Messages messages = Messages.getInstance(BuildResult.class);
        File contentRoot = systemPaths.getContentRoot();
        if (result.completed())
        {
            if (canWrite)
            {
                actions.add(ToveUtils.getActionLink(AccessManager.ACTION_DELETE, messages, contentRoot));
            }
        }
        else
        {
            if (accessManager.hasPermission(ProjectConfigurationActions.ACTION_CANCEL_BUILD, result))
            {
                actions.add(ToveUtils.getActionLink(BuildResult.ACTION_CANCEL, messages, contentRoot));
            }
        }

        ProjectResponsibility projectResponsibility = project.getResponsibility();
        if (projectResponsibility == null && accessManager.hasPermission(ProjectConfigurationActions.ACTION_TAKE_RESPONSIBILITY, project))
        {
            actions.add(ToveUtils.getActionLink(ProjectConfigurationActions.ACTION_TAKE_RESPONSIBILITY, messages, contentRoot));
        }

        if (projectResponsibility != null)
        {
            responsibleOwner = projectResponsibility.getMessage(getLoggedInUser());
            responsibleComment = projectResponsibility.getComment();

            if (accessManager.hasPermission(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY, project))
            {
                canClearResponsible = true;
                actions.add(ToveUtils.getActionLink(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY, messages, contentRoot));
            }
        }

        if (getLoggedInUser() != null)
        {
            actions.add(ToveUtils.getActionLink(BuildResult.ACTION_ADD_COMMENT, messages, contentRoot));
        }

        // Initialise detail down to the command level (optional)
        getCommandResult();

        File dataDir = configurationManager.getDataDirectory();
        result.loadFeatures(dataDir);

        if(result.completed())
        {
            result.loadFailedTestResults(dataDir, getFailureLimit());
            loadDependencyDetails(result);
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

    public String pushSuite(PersistentTestSuiteResult suite)
    {
        if(pathStack.empty())
        {
            return pathStack.push(uriComponentEncode(suite.getName()));
        }
        else
        {
            return pathStack.push(pathStack.peek() + "/" + uriComponentEncode(suite.getName()));
        }
    }

    public void popSuite()
    {
        pathStack.pop();
    }

    public List<PersistentChangelist> getChangelists()
    {
        if(changelists == null)
        {
            changelists = buildManager.getChangesForBuild(getResult(), true);
        }
        return changelists;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
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
                Urls urls = new Urls( configurationProvider.get(GlobalConfiguration.class).getBaseUrl());

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
                    boolean isDependencyAvailable = projectManager.getProjectConfig(mrid.getName(), false) != null;
                    if (isDependencyAvailable)
                    {
                        stageDependency.setProjectUrl(urls.project(uriComponentEncode(dependencyProjectName)));
                    }

                    String ivyPath = configuration.getIvyPath(mrid, mrid.getRevision());
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
