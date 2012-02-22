package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.api.CheckoutScheme;
import com.zutubi.pulse.master.build.queue.BuildQueueSnapshot;
import com.zutubi.pulse.master.build.queue.BuildRequestRegistry;
import com.zutubi.pulse.master.build.queue.QueuedRequest;
import com.zutubi.pulse.master.build.queue.SchedulingController;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.project.BootstrapConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions.*;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.master.tove.config.project.commit.CommitMessageTransformerConfiguration;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.links.ConfigurationLinks;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.*;
import static java.util.Arrays.asList;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An action that provides the JSON data for rendering a project home page.
 */
public class ProjectHomeDataAction extends ProjectActionBase
{
    private static final Messages PROJECT_I18N = Messages.getInstance(ProjectConfiguration.class);

    private static final String LINK_HOMEPAGE = "homepage";
    private static final String LINK_RSS = "rss";

    private ProjectHomeModel model;
    
    private ActionManager actionManager;
    private SystemPaths systemPaths;
    private SchedulingController schedulingController;
    private BuildRequestRegistry buildRequestRegistry;
    private ConfigurationManager configurationManager;

    public ProjectHomeModel getModel()
    {
        return model;
    }

    public String execute()
    {
        Project project = getRequiredProject();
        
        // Get queued builds first, so we can detect a race with a build moving
        // from queued to active.
        BuildQueueSnapshot snapshot = schedulingController.getSnapshot();
        List<QueuedRequest> queued = snapshot.getQueuedRequestsByOwner(project);
        List<BuildResult> inProgress = buildManager.queryBuilds(project, ResultState.getIncompleteStates(), -1, -1, -1, -1, true, false);
        List<BuildResult> latestCompleted = buildManager.getLatestCompletedBuildResults(project, 10);
        
        // Race detection: anything in our queue snapshot that corresponds to a
        // build number (in progress or even completed) should be filtered out,
        // that request must have been activate between snapshotting and
        // querying the builds.
        final Set<Long> activatedBuilds = new HashSet<Long>();
        CollectionUtils.map(inProgress, new BuildResultToNumberMapping(), activatedBuilds);
        CollectionUtils.map(latestCompleted, new BuildResultToNumberMapping(), activatedBuilds);
        queued = CollectionUtils.filter(queued, new Predicate<QueuedRequest>()
        {
            public boolean satisfied(QueuedRequest queuedRequest)
            {
                return !activatedBuilds.contains(buildRequestRegistry.getBuildNumber(queuedRequest.getRequest().getId()));
            }
        });

        Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
        ProjectConfiguration projectConfig = project.getConfig();
        BuildResult latestCompletedResult = latestCompleted.isEmpty() ? null : latestCompleted.get(0);
        BuildResultToModelMapping buildMapping = new BuildResultToModelMapping(urls, projectConfig.getChangeViewer());
        if (latestCompletedResult != null)
        {
            buildMapping.collectArtifactsForBuildId(latestCompletedResult.getId());
        }

        model = new ProjectHomeModel(createStatusModel(latestCompletedResult, urls), projectConfig.getOptions().getPrompt());
        addActivity(queued, inProgress, buildMapping);
        addRecent(latestCompleted, buildMapping);
        addChanges();
        addDescription(projectConfig);
        addLinks(urls);
        addActions();

        return SUCCESS;
    }

    private ProjectHomeModel.StatusModel createStatusModel(final BuildResult latestCompleted, final Urls urls)
    {
        Project project = getProject();

        Project.State projectState = project.getState();
        Project.Transition keyTransition = getKeyTransition(project, projectState);
        
        ProjectHomeModel.StateModel state = new ProjectHomeModel.StateModel(
                EnumUtils.toPrettyString(projectState),
                keyTransition == null ? null : EnumUtils.toPrettyString(keyTransition)
        );
        
        ProjectHomeModel.StatisticsModel statistics = new ProjectHomeModel.StatisticsModel(
                buildManager.getBuildCount(project, ResultState.getCompletedStates()),
                buildManager.getBuildCount(project, new ResultState[]{ResultState.SUCCESS}),
                buildManager.getBuildCount(project, new ResultState[]{ResultState.FAILURE})
        );
        
        List<BuildStageModel> brokenStages = null;
        if (latestCompleted != null)
        {
            List<RecipeResultNode> brokenNodes = CollectionUtils.filter(latestCompleted.getStages(), new Predicate<RecipeResultNode>()
            {
                public boolean satisfied(RecipeResultNode recipeResultNode)
                {
                    return !recipeResultNode.getResult().succeeded();
                }
            });

            if (!brokenNodes.isEmpty())
            {
                brokenStages = CollectionUtils.map(brokenNodes, new Mapping<RecipeResultNode, BuildStageModel>()
                {
                    public BuildStageModel map(RecipeResultNode recipeResultNode)
                    {
                        return new BuildStageModel(latestCompleted, recipeResultNode, urls, false);
                    }
                });
            }
        }
        
        return new ProjectHomeModel.StatusModel(project.getName(), EnumUtils.toPrettyString(ProjectHealth.getHealth(latestCompleted)), state, statistics, brokenStages);
    }

    private Project.Transition getKeyTransition(Project project, Project.State projectState)
    {
        Project.Transition transition = null;
        if (project.isTransitionValid(Project.Transition.PAUSE))
        {
            transition = Project.Transition.PAUSE;
        }
        else if (project.isTransitionValid(Project.Transition.RESUME))
        {
            transition = Project.Transition.RESUME;
        }
        else if (projectState == Project.State.INITIALISATION_FAILED)
        {
            transition = Project.Transition.INITIALISE;
        }

        if (transition != null && !projectManager.hasStateTransitionPermission(project, transition))
        {
            transition = null;
        }
        return transition;
    }

    private void addActivity(List<QueuedRequest> queued, List<BuildResult> inProgress, BuildResultToModelMapping buildMapping)
    {
        ProjectConfiguration projectConfig = getProject().getConfig();
        CollectionUtils.map(queued, new QueuedToBuildModelMapping(projectConfig.getName(), projectConfig.getChangeViewer()), model.getActivity());
        CollectionUtils.map(inProgress, buildMapping, model.getActivity());
    }

    private void addRecent(List<BuildResult> completed, BuildResultToModelMapping buildMapping)
    {
        CollectionUtils.map(completed, buildMapping, model.getRecent());
    }

    private void addDescription(ProjectConfiguration projectConfig)
    {
        String description = projectConfig.getDescription();
        if (StringUtils.stringSet(description))
        {
            model.setDescription(description);
        }
    }

    private void addLinks(Urls urls)
    {
        Project project = getProject();
        if (accessManager.hasPermission(AccessManager.ACTION_WRITE, project.getConfig()))
        {
            model.addLink(new ActionLink(urls.adminProject(project), PROJECT_I18N.format(AccessManager.ACTION_WRITE + ConfigurationLinks.KEY_SUFFIX_LABEL), AccessManager.ACTION_WRITE));
        }

        String url = project.getConfig().getUrl();
        if (StringUtils.stringSet(url))
        {
            model.setUrl(url);
            model.addLink(new ActionLink(url, PROJECT_I18N.format(LINK_HOMEPAGE + ConfigurationLinks.KEY_SUFFIX_LABEL), LINK_HOMEPAGE));
        }

        GlobalConfiguration globalConfig = configurationTemplateManager.getInstance(GlobalConfiguration.SCOPE_NAME, GlobalConfiguration.class);
        if (globalConfig.isRssEnabled())
        {
            model.addLink(new ActionLink(urls.base() + "rss.action?projectId=" + project.getId(), PROJECT_I18N.format(LINK_RSS + ConfigurationLinks.KEY_SUFFIX_LABEL),  LINK_RSS));
        }
    }

    private void addActions()
    {
        File contentRoot = systemPaths.getContentRoot();
        List<String> availableActions = actionManager.getActions(getProject().getConfig(), false, true);
        for (String candidateAction: asList(AccessManager.ACTION_WRITE, ACTION_MARK_CLEAN, ACTION_TRIGGER, ACTION_REBUILD))
        {
            if (availableActions.contains(candidateAction))
            {
                model.addAction(ToveUtils.getActionLink(candidateAction, PROJECT_I18N, contentRoot));
            }
        }

        addResponsibilityActions(PROJECT_I18N, contentRoot);
        addViewWorkingCopyAction(PROJECT_I18N, contentRoot);
    }

    private void addResponsibilityActions(Messages messages, File contentRoot)
    {
        Project project = getProject();
        ProjectResponsibility projectResponsibility = project.getResponsibility();
        if (projectResponsibility == null && accessManager.hasPermission(ACTION_TAKE_RESPONSIBILITY, project))
        {
            model.addAction(ToveUtils.getActionLink(ACTION_TAKE_RESPONSIBILITY, messages, contentRoot));
        }

        if (projectResponsibility != null)
        {
            String responsibleOwner = projectResponsibility.getMessage(getLoggedInUser());
            String responsibleComment = projectResponsibility.getComment();

            ProjectResponsibilityModel responsibilityModel = new ProjectResponsibilityModel(responsibleOwner, responsibleComment);
            model.setResponsibility(responsibilityModel);
            if (accessManager.hasPermission(ACTION_CLEAR_RESPONSIBILITY, project))
            {
                responsibilityModel.setCanClear(true);
                model.addAction(ToveUtils.getActionLink(ACTION_CLEAR_RESPONSIBILITY, messages, contentRoot));
            }
        }
    }

    private void addViewWorkingCopyAction(Messages messages, File contentRoot)
    {
        BootstrapConfiguration config = getProject().getConfig().getBootstrap();
        if (config != null && config.getCheckoutScheme() == CheckoutScheme.INCREMENTAL_UPDATE)
        {
            if (accessManager.hasPermission(ACTION_VIEW_SOURCE, getProject()))
            {
                model.addAction(ToveUtils.getActionLink(ACTION_VIEW_SOURCE, messages, contentRoot));
            }
        }
    }

    private void addChanges()
    {
        List<PersistentChangelist> latestChanges = buildManager.getLatestChangesForProject(getProject(), 10);
        ProjectConfiguration projectConfiguration = getProject().getConfig();
        final ChangeViewerConfiguration changeViewer = projectConfiguration.getChangeViewer();
        final Collection<CommitMessageTransformerConfiguration> transformers = projectConfiguration.getCommitMessageTransformers().values();
        CollectionUtils.map(latestChanges, new Mapping<PersistentChangelist, ChangelistModel>()
        {
            public ChangelistModel map(PersistentChangelist persistentChangelist)
            {
                return new ChangelistModel(persistentChangelist, changeViewer, transformers);
            }
        }, model.getChanges());
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    private static class QueuedToBuildModelMapping implements Mapping<QueuedRequest, BuildModel>
    {
        private String projectName;
        private ChangeViewerConfiguration changeViewerConfig;

        private QueuedToBuildModelMapping(String projectName, ChangeViewerConfiguration changeViewerConfig)
        {
            this.projectName = projectName;
            this.changeViewerConfig = changeViewerConfig;
        }

        public BuildModel map(QueuedRequest queuedRequest)
        {
            BuildRequestEvent requestEvent = queuedRequest.getRequest();
            Revision revision = requestEvent.getRevision().getRevision();
            RevisionModel revisionModel;
            if (revision == null)
            {
                revisionModel = new RevisionModel("[floating]");
            }
            else
            {
                revisionModel = new RevisionModel(revision, changeViewerConfig);
            }
            
            return new BuildModel(requestEvent.getId(), -1, false, projectName, projectName, "queued", requestEvent.getPrettyQueueTime(), requestEvent.getReason().getSummary(), revisionModel, requestEvent.getStatus(), requestEvent.getVersion());
        }
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }

    public void setSchedulingController(SchedulingController schedulingController)
    {
        this.schedulingController = schedulingController;
    }

    public void setBuildRequestRegistry(BuildRequestRegistry buildRequestRegistry)
    {
        this.buildRequestRegistry = buildRequestRegistry;
    }
}
