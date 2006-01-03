package com.cinnamonbob.scm;

import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.model.Scm;
import com.cinnamonbob.model.ScmManager;
import com.cinnamonbob.scheduling.Task;
import com.cinnamonbob.scheduling.TaskExecutionContext;
import com.cinnamonbob.util.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class MonitorScms implements Task
{
    private static final Map<Long, Revision> latestRevisions = new HashMap<Long, Revision>();

    private static final Logger LOG = Logger.getLogger(MonitorScms.class);

    private ScmManager scmManager;
    private EventManager eventManager;

    public void execute(TaskExecutionContext context)
    {
        //TODO: Add some form of profiling to monitor the amount of time spent 'checking scms'.
        //TODO: This could be a resource drain so we also need to make sure that this is
        //TODO: not run within a transaction and NOT being run more then once at a time.

        //TODO: may be useful to track the amount of time its tacking to 'check scms' and record
        //TODO: this for profiling...

        LOG.info("checking scms for changes.");
        for (Scm scm : scmManager.getActiveScms())
        {
            try
            {
                // when was the last time that we checked? if never, get the latest revision.
                SCMServer server = scm.createServer();
                if (!latestRevisions.containsKey(scm.getId()))
                {
                    latestRevisions.put(scm.getId(), server.getLatestRevision());
                }
                Revision previous = latestRevisions.get(scm.getId());
                if (server.hasChangedSince(previous))
                {
                    Revision latest = server.getLatestRevision();
                    eventManager.publish(new SCMChangeEvent(scm, latest, previous));
                    latestRevisions.put(scm.getId(), latest);
                }
            }
            catch (SCMException e)
            {
                LOG.severe(e);
            }
        }
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
