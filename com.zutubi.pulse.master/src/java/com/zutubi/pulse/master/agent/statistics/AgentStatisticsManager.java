package com.zutubi.pulse.master.agent.statistics;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.agent.AgentStatus;
import com.zutubi.pulse.master.events.AgentStatusChangeEvent;
import com.zutubi.pulse.master.model.AgentDailyStatistics;
import com.zutubi.pulse.master.model.persistence.AgentDailyStatisticsDao;
import com.zutubi.pulse.master.scheduling.CallbackService;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Constants;
import com.zutubi.util.Mapping;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.time.Clock;
import com.zutubi.util.time.SystemClock;

import java.util.*;

/**
 * Keeps agent statistics up to date by listening to agent status changes.
 * Requires regular calls to {@link #update()} to ensure that not too much data
 * is lost when the master is shut down. 
 */
public class AgentStatisticsManager implements EventListener
{
    private static final Logger LOG = Logger.getLogger(AgentStatisticsManager.class);

    private static final long TRIGGER_INTERVAL = 5 * Constants.MINUTE;
    private static final String CALLBACK_NAME = "Agent Statistics";

    private Map<Long, StampedStatus> agentIdToStatus = new HashMap<Long, StampedStatus>();
    private long todayStamp;
    private long tomorrowStamp;

    private AgentDailyStatisticsDao agentDailyStatisticsDao;
    private AgentManager agentManager;
    private Clock clock = new SystemClock();
    private CallbackService callbackService;

    public synchronized void init()
    {
        calculateStamps(clock.getCurrentTimeMillis());
        ensureTriggerScheduled();

        // We rely on the transition from the initial state to bootstrap us for
        // each agent.
    }

    private void ensureTriggerScheduled()
    {
        try
        {
            callbackService.registerCallback(CALLBACK_NAME, new Runnable()
            {
                public void run()
                {
                    agentManager.updateStatistics();
                }
            }, TRIGGER_INTERVAL);
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }
    }

    public synchronized void update()
    {
        try
        {
            long timeNow = clock.getCurrentTimeMillis();
            if (timeNow > tomorrowStamp)
            {
                rollover(timeNow);
            }

            collectGarbage();
            updateToTime(timeNow);
        }
        finally
        {
            // Ensure changes to effect before the lock is released.
            agentDailyStatisticsDao.flush();
        }
    }

    private void collectGarbage()
    {
        // Wipe out entries that are too old.
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        zeroTime(calendar);
        agentDailyStatisticsDao.deleteByDayStampBefore(calendar.getTimeInMillis());

        // And those that are for agents that no longer exist.
        Set<Long> allIds = new HashSet<Long>();
        CollectionUtils.map(agentManager.getAllAgents(), new Mapping<Agent, Long>()
        {
            public Long map(Agent agent)
            {
                return agent.getId();
            }
        }, allIds);

        agentDailyStatisticsDao.deleteByAgentNotIn(allIds);
    }

    public synchronized List<AgentDailyStatistics> getStatisticsForAgent(long agentId)
    {
        return agentDailyStatisticsDao.findByAgent(agentId);
    }

    private void calculateStamps(long timeNow)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeNow);
        zeroTime(calendar);
        todayStamp = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        tomorrowStamp = calendar.getTimeInMillis();
    }

    private void zeroTime(Calendar calendar)
    {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void handleStatusChange(AgentStatusChangeEvent event)
    {
        long agentId = event.getAgent().getId();
        long timestamp = clock.getCurrentTimeMillis();
        if (timestamp > tomorrowStamp)
        {
            rollover(timestamp);
        }

        StampedStatus current = agentIdToStatus.get(agentId);
        if (current == null)
        {
            agentIdToStatus.put(agentId, new StampedStatus(event.getNewStatus(), timestamp));
        }
        else
        {
            updateStatistics(agentId, current.status, (int) (timestamp - current.time), event.getNewStatus() == AgentStatus.RECIPE_ASSIGNED);
            current.status = event.getNewStatus();
            current.time = timestamp;
        }
    }

    private void updateStatistics(long agentId, AgentStatus status, int duration, boolean newRecipe)
    {
        AgentDailyStatistics currentStats = agentDailyStatisticsDao.safeFindByAgentAndDay(agentId, todayStamp);
        if (currentStats == null)
        {
            currentStats = new AgentDailyStatistics(agentId, todayStamp);
        }

        switch (status)
        {
            case DISABLED:
                currentStats.setDisabledTime(currentStats.getDisabledTime() + duration);
                break;
            case INITIAL:
            case INVALID_MASTER:
            case OFFLINE:
            case TOKEN_MISMATCH:
            case VERSION_MISMATCH:
                currentStats.setOfflineTime(currentStats.getOfflineTime() + duration);
                break;
           case SYNCHRONISING:
           case SYNCHRONISED:
               currentStats.setSynchronisingTime(currentStats.getSynchronisingTime() + duration);
               break;
            case IDLE:
                currentStats.setIdleTime(currentStats.getIdleTime() + duration);
                break;
            case AWAITING_PING:
            case BUILDING:
            case BUILDING_INVALID:
            case POST_RECIPE:
            case RECIPE_ASSIGNED:
            case RECIPE_DISPATCHED:
                currentStats.setBusyTime(currentStats.getBusyTime() + duration);
                break;
        }

        if (newRecipe)
        {
            currentStats.setRecipeCount(currentStats.getRecipeCount() + 1);
        }

        agentDailyStatisticsDao.save(currentStats);
    }

    private void rollover(long timeNow)
    {
        updateToTime(tomorrowStamp);
        calculateStamps(timeNow);
    }

    private void updateToTime(long time)
    {
        for (Map.Entry<Long, StampedStatus> entry: agentIdToStatus.entrySet())
        {
            StampedStatus current = entry.getValue();
            updateStatistics(entry.getKey(), current.status, (int) (time - current.time), false);
            current.time = time;
        }
    }

    public synchronized void handleEvent(Event event)
    {
        try
        {
            handleStatusChange((AgentStatusChangeEvent) event);
        }
        finally
        {
            // Ensure changes to effect before the lock is released.
            agentDailyStatisticsDao.flush();
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{AgentStatusChangeEvent.class};
    }

    public void setAgentDailyStatisticsDao(AgentDailyStatisticsDao agentDailyStatisticsDao)
    {
        this.agentDailyStatisticsDao = agentDailyStatisticsDao;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setClock(Clock clock)
    {
        this.clock = clock;
    }

    public void setCallbackService(CallbackService callbackService)
    {
        this.callbackService = callbackService;
    }

    private static class StampedStatus
    {
        private AgentStatus status;
        private long time;

        private StampedStatus(AgentStatus status, long time)
        {
            this.status = status;
            this.time = time;
        }
    }
}
