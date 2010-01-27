package com.zutubi.pulse.master.scheduling;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.model.persistence.TriggerDao;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.ConjunctivePredicate;
import com.zutubi.util.logging.Logger;

import java.util.*;

/**
 * The implementation of the scheduler interface.  It provides persistence of
 * triggers and delegates the handling of the triggers to the appropriate
 * registered SchedulerStrategy instances.
 */
public class DefaultScheduler implements Scheduler
{
    private static final Logger LOG = Logger.getLogger(DefaultScheduler.class);

    private Map<String, SchedulerStrategy> strategies = new TreeMap<String, SchedulerStrategy>();

    private TriggerDao triggerDao;

    /**
     * The list of triggers that are not persistent across restarts.  Any state changes
     * made to these triggers is lost on restart.
     */
    private List<Trigger> transientTriggers = new LinkedList<Trigger>();

    private volatile boolean started = false;

    /**
     * Register a scheduling strategy that will be used to handle a specific trigger type.
     *
     * @param strategy new scheduler strategy implementation
     */
    public void register(SchedulerStrategy strategy)
    {
        for (String key : strategy.canHandle())
        {
            strategies.put(key, strategy);
        }
    }

    /**
     * Update the list of registered strategies to be the list of strategies specified.
     * Any strategies previously registered will be dropped.
     *
     * @param strategies the new list of strategies to be registered.
     * @see #register(SchedulerStrategy)
     */
    public void setStrategies(SchedulerStrategy... strategies)
    {
        setStrategies(Arrays.asList(strategies));
    }

    /**
     * Update the list of registered strategies to be the list of strategies specified.
     * Any strategies previously registered will be dropped.
     *
     * @param strategies the new list of strategies to be registered.
     * @see #register(SchedulerStrategy)
     */
    public void setStrategies(List<SchedulerStrategy> strategies)
    {
        this.strategies.clear();
        for (SchedulerStrategy strategy : strategies)
        {
            register(strategy);
        }
    }

    public synchronized void start()
    {
        try
        {
            // initialise the persisted triggers.
            for (Trigger trigger : triggerDao.findAll())
            {
                if (isTriggerValid(trigger))
                {
                    try
                    {
                        getStrategy(trigger).init(trigger);
                    }
                    catch (SchedulingException e)
                    {
                        // not the fact that this trigger is invalid but do not prevent the rest of the triggers from
                        // being initialised.
                        LOG.severe("Failed to initialise a trigger (" + trigger.getGroup() + ", " + trigger.getName() + ")", e);
                    }
                }
            }

            // initialise the already registered transient triggers.
            for (Trigger trigger : transientTriggers)
            {
                try
                {
                    getStrategy(trigger).init(trigger);
                }
                catch (SchedulingException e)
                {
                    LOG.severe("Failed to initialise a trigger (" + trigger.getGroup() + ", " + trigger.getName() + ")", e);
                }
            }
        }
        finally
        {
            started = true;
        }
    }

    private boolean isTriggerValid(Trigger trigger)
    {
        if (trigger.getProjectId() != 0 && trigger.getConfig() == null)
        {
            LOG.warning("Project trigger '" + trigger.getName() + "' from group '" + trigger.getGroup() + "' has no configuration (id: " + trigger.getId() + ", project id: " + trigger.getProjectId() + ")");
            return false;
        }

        return true;
    }

    public Trigger getTrigger(String name, String group)
    {
        Trigger trigger = CollectionUtils.find(transientTriggers, new HasNameAndGroupPredicate(name, group));
        if (trigger != null)
        {
            return trigger;
        }
        return triggerDao.findByNameAndGroup(name, group);
    }

    public Trigger getTrigger(long id)
    {
        return triggerDao.findById(id);
    }

    public List<Trigger> getTriggers()
    {
        return CollectionUtils.concatenate(
                triggerDao.findAll(),
                transientTriggers);
    }

    public List<Trigger> getTriggers(long project)
    {
        return CollectionUtils.concatenate(
                triggerDao.findByProject(project),
                CollectionUtils.filter(transientTriggers, new HasProjectPredicate(project)));
    }

    public List<Trigger> getTriggers(String group)
    {
        return CollectionUtils.concatenate(
                triggerDao.findByGroup(group),
                CollectionUtils.filter(transientTriggers, new HasGroupPredicate(group)));
    }

    public Trigger getTrigger(long project, String triggerName)
    {
        Trigger trigger = CollectionUtils.find(transientTriggers, new ConjunctivePredicate<Trigger>(
                new HasProjectPredicate(project), new HasNamePredicate(triggerName)
        ));
        if (trigger != null)
        {
            return trigger;
        }
        return triggerDao.findByProjectAndName(project, triggerName);
    }

    public void schedule(Trigger trigger) throws SchedulingException
    {
        if (getTrigger(trigger.getName(), trigger.getGroup()) != null)
        {
            throw new SchedulingException("A trigger with name " + trigger.getName() + " and group " + trigger.getGroup() + " has already been scheduled.");
        }

        if (trigger.isScheduled())
        {
            throw new SchedulingException("Trigger is already scheduled.");
        }

        SchedulerStrategy impl = getStrategy(trigger);
        if (impl == null)
        {
            throw new SchedulingException("No strategy associated with trigger of type " + trigger.getType());
        }

        trigger.setState(TriggerState.SCHEDULED);
        if (trigger.isTransient())
        {
            transientTriggers.add(trigger);
        }
        else
        {
            triggerDao.save(trigger);    
        }

        if (started)
        {
            impl.schedule(trigger);
        }
    }

    public void unschedule(Trigger trigger) throws SchedulingException
    {
        assertScheduled(trigger);
        SchedulerStrategy impl = getStrategy(trigger);
        impl.unschedule(trigger);

        trigger.setState(TriggerState.NONE);

        if (trigger.isTransient())
        {
            transientTriggers.remove(trigger);
        }
        else
        {
            triggerDao.delete(trigger);
        }
    }

    public void update(Trigger trigger) throws SchedulingException
    {
        assertScheduled(trigger);

        if (!trigger.isTransient())
        {
            triggerDao.save(trigger);
        }

        TriggerState state = trigger.getState();

        SchedulerStrategy impl = getStrategy(trigger);
        impl.unschedule(trigger);

        switch (state)
        {
            case SCHEDULED:
                impl.schedule(trigger);
                break;
            case PAUSED:
                impl.pause(trigger);
                break;
        }
    }

    public void pause(String group) throws SchedulingException
    {
        for (Trigger trigger : getTriggers(group))
        {
            pause(trigger);
        }
    }

    public void pause(Trigger trigger) throws SchedulingException
    {
        assertScheduled(trigger);
        if (!trigger.isActive())
        {
            return;
        }

        trigger.setState(TriggerState.PAUSED);
        if (!trigger.isTransient())
        {
            triggerDao.save(trigger);
        }

        SchedulerStrategy impl = getStrategy(trigger);
        impl.pause(trigger);
    }

    public void resume(String group) throws SchedulingException
    {
        for (Trigger trigger : getTriggers(group))
        {
            resume(trigger);
        }
    }

    public void resume(Trigger trigger) throws SchedulingException
    {
        assertScheduled(trigger);
        if (!trigger.isPaused())
        {
            return;
        }

        trigger.setState(TriggerState.SCHEDULED);
        if (!trigger.isTransient())
        {
            triggerDao.save(trigger);
        }

        SchedulerStrategy impl = getStrategy(trigger);
        impl.resume(trigger);
    }

    /**
     * Retrieve the first registered strategy that is able to handle this trigger.
     *
     * @param trigger instance for which the strategy is being retrieved.
     * @return a scheduler strategy.
     */
    private SchedulerStrategy getStrategy(Trigger trigger)
    {
        return strategies.get(trigger.getType());
    }

    private void assertScheduled(Trigger trigger) throws SchedulingException
    {
        if (!isScheduled(trigger))
        {
            throw new SchedulingException("The trigger must be scheduled.");
        }
    }

    private boolean isScheduled(Trigger trigger) throws SchedulingException
    {
        if (trigger.isTransient())
        {
            return getTrigger(trigger.getName(), trigger.getGroup()) != null;
        }
        else
        {
            return trigger.isScheduled();
        }
    }

    /**
     * Set a reference to the required TriggerDao resource.
     *
     * @param triggerDao instance
     */
    public void setTriggerDao(TriggerDao triggerDao)
    {
        this.triggerDao = triggerDao;
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(new SystemStartedListener()
        {
            public void systemStarted()
            {
                start();
            }
        });
    }

    public void stop(boolean force)
    {
        if (!started)
        {
            // nothing to do here.
            return;
        }

        try
        {
            for (SchedulerStrategy strategy : strategies.values())
            {
                strategy.stop(force);
            }
        }
        finally
        {
            started = false;
        }
    }
}
