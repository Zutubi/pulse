package com.zutubi.pulse.scm;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.model.Cvs;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.model.ScmManager;
import com.zutubi.pulse.scheduling.Task;
import com.zutubi.pulse.scheduling.TaskExecutionContext;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.util.Pair;
import com.zutubi.pulse.util.Constants;

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

    private static final Map<Long, Pair<Long, Revision>> waiting = new HashMap<Long, Pair<Long, Revision>>();

    //TODO: Add some form of profiling to monitor the amount of time spent 'checking scms'.
    //TODO: This could be a resource drain so we also need to make sure that this is
    //TODO: not run within a transaction and NOT being run more then once at a time.

    //TODO: may be useful to track the amount of time its tacking to 'check scms' and record
    //TODO: this for profiling...

    public void execute(TaskExecutionContext context)
    {
        LOG.entering(MonitorScms.class.getName(), "execute");

        for (Scm scm : scmManager.getActiveScms())
        {
            try
            {
                long now = System.currentTimeMillis();

                // A) is it time to poll this scm server?
                if (scm.getLastPollTime() != null)
                {
                    // poll interval.
                    int pollingInterval = scmManager.getDefaultPollingInterval();
                    if (scm.getPollingInterval() != null)
                    {
                        pollingInterval = scm.getPollingInterval();
                    }

                    long lastPollTime = scm.getLastPollTime();
                    long nextPollTime = lastPollTime + Constants.MINUTE * pollingInterval;

                    if (now < nextPollTime)
                    {
                        continue;
                    }
                }

                // set the poll time.
                scm.setLastPollTime(now);
                scmManager.save(scm);

                // when was the last time that we checked? if never, get the latest revision.
                SCMServer server = scm.createServer();
                if (!latestRevisions.containsKey(scm.getId()))
                {
                    latestRevisions.put(scm.getId(), server.getLatestRevision());
                    continue;
                }

                Revision previous = latestRevisions.get(scm.getId());

                // We need to move this CVS specific code into the cvs implementation specific code.
                if (scm instanceof Cvs)
                {
                    Cvs cvs = (Cvs) scm;
                    // are we waiting
                    if (waiting.containsKey(scm.getId()))
                    {
                        long quietTime = waiting.get(scm.getId()).first;
                        if (quietTime < System.currentTimeMillis())
                        {
                            if (server.hasChangedSince(waiting.get(scm.getId()).second))
                            {
                                // there has been a commit during the 'quiet period', lets reset the timer.
                                Revision latest = server.getLatestRevision();
                                waiting.put(scm.getId(), new Pair<Long, Revision>(System.currentTimeMillis() + cvs.getQuietPeriod(), latest));
                            }
                            else
                            {
                                // there have been no commits during the 'quiet period', trigger a change.
                                Revision latest = server.getLatestRevision();
                                eventManager.publish(new SCMChangeEvent(scm, latest, previous));
                                latestRevisions.put(scm.getId(), latest);
                                waiting.remove(scm.getId());
                            }
                        }
                    }
                    else
                    {
                        if (server.hasChangedSince(previous))
                        {
                            Revision latest = server.getLatestRevision();
                            if (cvs.getQuietPeriod() != 0)
                            {
                                waiting.put(scm.getId(), new Pair<Long, Revision>(System.currentTimeMillis() + cvs.getQuietPeriod(), latest));
                            }
                            else
                            {
                                eventManager.publish(new SCMChangeEvent(scm, latest, previous));
                                latestRevisions.put(scm.getId(), latest);
                            }
                        }
                    }
                }
                else
                {
                    if (server.hasChangedSince(previous))
                    {
                        Revision latest = server.getLatestRevision();
                        LOG.finer("publishing scm change event for " + scm + " revision " + latest);
                        eventManager.publish(new SCMChangeEvent(scm, latest, previous));
                        latestRevisions.put(scm.getId(), latest);
                    }
                }
            }
            catch (SCMException e)
            {
                // there has been a problem communicating with one of the scms. Log the
                // warning and move on.
                // This needs to be brought to the attention of the user since its likely to
                // be the result of a configuration problem.
                LOG.warning(e.getMessage());
            }
        }
        LOG.exiting();
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
