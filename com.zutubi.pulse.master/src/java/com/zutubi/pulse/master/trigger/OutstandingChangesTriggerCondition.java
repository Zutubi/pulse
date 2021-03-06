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

package com.zutubi.pulse.master.trigger;

import com.google.common.base.Predicate;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.master.build.queue.FatController;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildRevision;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.triggers.OutstandingChangesTriggerConditionConfiguration;
import com.zutubi.util.logging.Logger;

import java.util.List;

import static com.google.common.collect.Iterables.find;

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
        Revision previousRevision = null;
        if (config.isCheckQueued())
        {
            BuildRevision latestQueued = getLatestQueuedRevision(project);
            if (latestQueued != null)
            {
                if (latestQueued.isInitialised())
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

        if (previousRevision == null)
        {
            previousRevision = buildManager.getPreviousRevision(project);
        }
        
        return previousRevision == null || hasChangedSince(project, previousRevision);
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
        BuildRequestEvent event = find(queued, new Predicate<BuildRequestEvent>()
        {
            public boolean apply(BuildRequestEvent event)
            {
                return !event.getRevision().isUser();
            }
        }, null);

        return event == null ? null : event.getRevision();
    }

    private boolean hasChangedSince(Project project, final Revision latestBuiltRevision)
    {
        try
        {
            return ScmClientUtils.withScmClient(project.getConfig(), project.getState(), scmManager, new ScmClientUtils.ScmContextualAction<Boolean>()
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
