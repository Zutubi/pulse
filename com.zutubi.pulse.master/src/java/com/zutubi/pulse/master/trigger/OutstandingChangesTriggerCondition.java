package com.zutubi.pulse.master.trigger;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.master.FatController;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.OutstandingChangesTriggerConditionConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.logging.Logger;

import java.util.List;

/**
 * A trigger condition that checks if there will be any new changes in a build
 * if its revision were fixed now.  If so, the condition is passed.  Checking
 * queued/active builds is optional.
 */
public class OutstandingChangesTriggerCondition extends TriggerConditionSupport
{
    private static final Logger LOG = Logger.getLogger(OutstandingChangesTriggerCondition.class);

    private BuildManager buildManager;
    private FatController fatController;
    private ScmManager scmManager;

    public OutstandingChangesTriggerCondition(OutstandingChangesTriggerConditionConfiguration config)
    {
        super(config);
    }

    public boolean satisfied(Project project)
    {
        OutstandingChangesTriggerConditionConfiguration config = (OutstandingChangesTriggerConditionConfiguration) getConfig();
        Revision previousRevision = buildManager.getPreviousRevision(project);
        if (config.isCheckQueued())
        {
            BuildRevision latestQueued = getLatestQueuedRevision(project);
            if (latestQueued != null)
            {
                if (isRevisionFixed(latestQueued))
                {
                    previousRevision = latestQueued.getRevision();
                }
                else
                {
                    // The floating revision will certainly consume any
                    // outstanding changes.
                    return false;
                }
            }
        }

        return previousRevision == null || hasChangedSince(project.getConfig(), previousRevision);
    }

    private boolean isRevisionFixed(BuildRevision latestQueued)
    {
        // There is a race where the revision may be fixed just after we check,
        // but there is no sense worrying about it -- this condition is just
        // making its best assessment at the current time.
        latestQueued.lock();
        try
        {
            return latestQueued.isFixed();
        }
        finally
        {
            latestQueued.unlock();
        }
    }

    /**
     * Finds the queued build that, presuming it is not cancelled, would be the
     * previous build for this project.  This is the last-queued one that has
     * not got a user-specified revision.
     *
     * @param project project to find the revision for
     * @return revision for the last queued or active build, excluding user
     *         revisions, or null if there are no such builds queued or active
     */
    private BuildRevision getLatestQueuedRevision(Project project)
    {
        List<BuildRequestEvent> queued = fatController.getRequestsForEntity(project);
        BuildRequestEvent event = CollectionUtils.find(queued, new Predicate<BuildRequestEvent>()
        {
            public boolean satisfied(BuildRequestEvent event)
            {
                return !event.getRevision().isUser();
            }
        });

        return event == null ? null : event.getRevision();
    }

    private boolean hasChangedSince(ProjectConfiguration projectConfig, final Revision latestBuiltRevision)
    {
        try
        {
            return ScmClientUtils.withScmClient(projectConfig, scmManager, new ScmClientUtils.ScmContextualAction<Boolean>()
            {
                public Boolean process(ScmClient client, ScmContext context) throws ScmException
                {
                    if (client.getCapabilities(context).contains(ScmCapability.REVISIONS))
                    {
                        return client.getRevisions(context, latestBuiltRevision, null).size() > 0;
                    }
                    else
                    {
                        LOG.warning("Attempt to use outstanding changes condition with SCM that does not support revisions");
                        return false;
                    }
                }
            });
        }
        catch (ScmException e)
        {
            LOG.severe(e);
            return false;
        }
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setFatController(FatController fatController)
    {
        this.fatController = fatController;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
