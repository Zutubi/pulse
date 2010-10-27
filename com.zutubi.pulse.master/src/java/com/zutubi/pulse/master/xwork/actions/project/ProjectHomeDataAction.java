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
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.BuildResultToNumberMapping;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectResponsibility;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions.*;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An action that provides the JSON data for rendering a project home page.
 */
public class ProjectHomeDataAction extends ProjectActionBase
{
    private static final String LINK_HOMEPAGE = "homepage";

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

        ProjectHomeModel.StateModel state = new ProjectHomeModel.StateModel(EnumUtils.toPrettyString(project.getState()), project.isTransitionValid(Project.Transition.PAUSE), project.isTransitionValid(Project.Transition.RESUME));
        ProjectHomeModel.StatisticsModel statistics = new ProjectHomeModel.StatisticsModel(
                buildManager.getBuildCount(project, ResultState.getCompletedStates()),
                buildManager.getBuildCount(project, new ResultState[]{ResultState.SUCCESS}),
                buildManager.getBuildCount(project, new ResultState[]{ResultState.FAILURE})
        ); 
        
        // Get queued builds first, so we can detect a race with a build moving
        // from queued to active.
        BuildQueueSnapshot snapshot = schedulingController.getSnapshot();
        List<QueuedRequest> queued = snapshot.getQueuedRequestsByOwner(project);
        List<BuildResult> inProgress = buildManager.queryBuilds(project, ResultState.getIncompleteStates(), -1, -1, -1, -1, true, false);
        List<BuildResult> completed = buildManager.getLatestCompletedBuildResults(project, 11);
        BuildResult latest = completed.isEmpty() ? null : completed.remove(0);
        
        // Race detection: anything in our queue snapshot that corresponds to a
        // build number (in progress or even completed) should be filtered out,
        // that request must have been activate between snapshotting and
        // querying the builds.
        final Set<Long> activatedBuilds = new HashSet<Long>();
        CollectionUtils.map(inProgress, new BuildResultToNumberMapping(), activatedBuilds);
        CollectionUtils.map(completed, new BuildResultToNumberMapping(), activatedBuilds);
        queued = CollectionUtils.filter(queued, new Predicate<QueuedRequest>()
        {
            public boolean satisfied(QueuedRequest queuedRequest)
            {
                return !activatedBuilds.contains(buildRequestRegistry.getBuildNumber(queuedRequest.getRequest().getId()));
            }
        });

        ProjectHomeModel.StatusModel status = new ProjectHomeModel.StatusModel(project.getName(), EnumUtils.toPrettyString(ProjectHealth.fromLatestBuild(latest)), state, statistics); 
        model = new ProjectHomeModel(status);

        final ProjectConfiguration projectConfig = project.getConfig();
        CollectionUtils.map(queued, new QueuedToBuildModelMapping(projectConfig.getChangeViewer()), model.getActivity());
        BuildResultToModelMapping toModelMapping = new BuildResultToModelMapping(projectConfig.getChangeViewer());
        CollectionUtils.map(inProgress, toModelMapping, model.getActivity());

        if (latest != null)
        {
            model.setLatest(toModelMapping.map(latest));
        }

        CollectionUtils.map(completed, toModelMapping, model.getRecent());
        
        List<PersistentChangelist> latestChanges = buildManager.getLatestChangesForProject(project, 10);
        CollectionUtils.map(latestChanges, new Mapping<PersistentChangelist, ProjectHomeModel.ChangelistModel>()
        {
            public ProjectHomeModel.ChangelistModel map(PersistentChangelist persistentChangelist)
            {
                return new ProjectHomeModel.ChangelistModel(persistentChangelist, projectConfig);
            }
        }, model.getChanges());
        
//        User user = getLoggedInUser();
//        summaryColumns = new BuildColumns(user == null ? UserPreferencesConfiguration.defaultProjectColumns() : user.getPreferences().getProjectSummaryColumns());
//        recentColumns = new BuildColumns(user == null ? UserPreferencesConfiguration.defaultProjectColumns() : user.getPreferences().getProjectRecentColumns());

        File contentRoot = systemPaths.getContentRoot();
        Messages messages = Messages.getInstance(ProjectConfiguration.class);
        Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
        if (accessManager.hasPermission(AccessManager.ACTION_WRITE, projectConfig))
        {
            model.addLink(new ActionLink(urls.adminProject(project), messages.format(AccessManager.ACTION_WRITE + ConfigurationLinks.KEY_SUFFIX_LABEL), AccessManager.ACTION_WRITE));
        }

        String url = projectConfig.getUrl();
        if (StringUtils.stringSet(url))
        {
            model.setUrl(url);
            model.addLink(new ActionLink(url, messages.format(LINK_HOMEPAGE + ConfigurationLinks.KEY_SUFFIX_LABEL), LINK_HOMEPAGE));
        }

        List<String> availableActions = actionManager.getActions(projectConfig, false, true);
        for (String candidateAction: asList(AccessManager.ACTION_WRITE, ACTION_MARK_CLEAN, ACTION_TRIGGER, ACTION_REBUILD))
        {
            if (availableActions.contains(candidateAction))
            {
                model.addAction(ToveUtils.getActionLink(candidateAction, messages, contentRoot));
            }
        }

        addResponsibilityActions(project, messages, contentRoot);
        addViewWorkingCopyAction(project, messages, contentRoot);

        return SUCCESS;
    }

    private void addResponsibilityActions(Project project, Messages messages, File contentRoot)
    {
        ProjectResponsibility projectResponsibility = project.getResponsibility();
        if (projectResponsibility == null && accessManager.hasPermission(ACTION_TAKE_RESPONSIBILITY, project))
        {
            model.addAction(ToveUtils.getActionLink(ACTION_TAKE_RESPONSIBILITY, messages, contentRoot));
        }

        if (projectResponsibility != null)
        {
            String responsibleOwner = projectResponsibility.getMessage(getLoggedInUser());
            String responsibleComment = projectResponsibility.getComment();

            ProjectHomeModel.ProjectResponsibilityModel responsibilityModel = new ProjectHomeModel.ProjectResponsibilityModel(responsibleOwner, responsibleComment);
            model.setResponsibility(responsibilityModel);
            if (accessManager.hasPermission(ACTION_CLEAR_RESPONSIBILITY, project))
            {
                responsibilityModel.setCanClear(true);
                model.addAction(ToveUtils.getActionLink(ACTION_CLEAR_RESPONSIBILITY, messages, contentRoot));
            }
        }
    }

    private void addViewWorkingCopyAction(Project project, Messages messages, File contentRoot)
    {
        ProjectConfiguration config = project.getConfig();
        if (config.getScm().getCheckoutScheme() == CheckoutScheme.INCREMENTAL_UPDATE)
        {
            if (accessManager.hasPermission(ACTION_VIEW_SOURCE, project))
            {
                model.addAction(ToveUtils.getActionLink(ACTION_VIEW_SOURCE, messages, contentRoot));
            }
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    private static class QueuedToBuildModelMapping implements Mapping<QueuedRequest, BuildModel>
    {
        private ChangeViewerConfiguration changeViewerConfig;

        private QueuedToBuildModelMapping(ChangeViewerConfiguration changeViewerConfig)
        {
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
            
            return new BuildModel(requestEvent.getId(), -1, "queued", requestEvent.getPrettyQueueTime(), requestEvent.getReason().getSummary(), revisionModel);
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
