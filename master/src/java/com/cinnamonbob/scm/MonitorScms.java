package com.cinnamonbob.scm;

import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.core.util.Constants;
import com.cinnamonbob.events.EventManager;
import com.cinnamonbob.model.Scm;
import com.cinnamonbob.model.ScmManager;
import com.cinnamonbob.model.Cvs;
import com.cinnamonbob.scheduling.Task;
import com.cinnamonbob.scheduling.TaskExecutionContext;
import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.scm.cvs.CvsServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;

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
                    continue;
                }

                Revision previous = latestRevisions.get(scm.getId());

                // if scm is cvs, then we implement a quiet period.
                if (server.hasChangedSince(previous))
                {
                    if (scm instanceof Cvs)
                    {
                        Date latestUpdate = ((CvsServer)server).getLatestUpdate(null, previous.getDate());
                        // if that update is more then the quiet period ago, then trigger an event.
                        long now = System.currentTimeMillis();
                        if (now - latestUpdate.getTime() > ((Cvs)scm).getQuietPeriod())
                        {
                            Revision latest = server.getLatestRevision();
                            eventManager.publish(new SCMChangeEvent(scm, latest, previous));
                            latestRevisions.put(scm.getId(), latest);
                        }
                    }
                    else
                    {
                        Revision latest = server.getLatestRevision();
                        eventManager.publish(new SCMChangeEvent(scm, latest, previous));
                        latestRevisions.put(scm.getId(), latest);
                    }
                }
            }
            catch (SCMException e)
            {
                LOG.severe(e);
            }
        }
    }

    /**
     * Required resource.
     *
     * @param scmManager
     */
    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    /**
     * Required resource.
     *
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
