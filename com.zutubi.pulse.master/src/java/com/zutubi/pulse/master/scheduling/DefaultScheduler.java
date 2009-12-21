package com.zutubi.pulse.master.scheduling;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.model.persistence.TriggerDao;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;
import com.zutubi.util.CollectionUtils;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.NullaryProcedure;
import com.zutubi.util.Pair;
import com.zutubi.util.Predicate;
import com.zutubi.util.logging.Logger;

import java.util.*;

/**
 * The implementation of the scheduler interface.  It provides persistence of
 * triggers and delegates the handling of the triggers to the appropriate
 * registered SchedulerStrategy instances.
 */
public class DefaultScheduler implements Scheduler
{
    private static final String CALLBACK_TRIGGER_GROUP = "callback-triggers";

    private static final Logger LOG = Logger.getLogger(DefaultScheduler.class);

    private Map<String, SchedulerStrategy> strategies = new TreeMap<String, SchedulerStrategy>();

    private TriggerHandler triggerHandler;

    private TriggerDao triggerDao;

    private Map<String, Pair<NullaryProcedure, Trigger>> registeredCallbacks = new HashMap<String, Pair<NullaryProcedure, Trigger>>();
    private List<Pair<NullaryProcedure, Trigger>> callbacksToRegister = new LinkedList<Pair<NullaryProcedure, Trigger>>();
    private long nextCallbackId = 1;

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
        strategy.setTriggerHandler(triggerHandler);
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

    public void start()
    {
        try
        {
            // ensure that the strategies are correctly configured.
            for (SchedulerStrategy strategy : strategies.values())
            {
                strategy.setTriggerHandler(triggerHandler);
            }

            // initialise the persisted triggers.
            for (Trigger trigger : triggerDao.findAll())
            {
                SchedulerStrategy strategy = getStrategy(trigger);
                try
                {
                    strategy.init(trigger);
                }
                catch (SchedulingException e)
                {
                    // not the fact that this trigger is invalid but do not prevent the rest of the triggers from
                    // being initialised.
                    LOG.severe("Failed to initialise a trigger (" + trigger.getGroup() + ", " + trigger.getName() + ")", e);
                }
            }

            // initialise the already registered callbacks.
            for (Pair<NullaryProcedure, Trigger> value : callbacksToRegister)
            {
                Trigger trigger = value.getSecond();
                try
                {
                    registeredCallbacks.put(trigger.getName(), value);
                    getStrategy(trigger).schedule(trigger);
                }
                catch (SchedulingException e)
                {
                    LOG.severe("Failed to initialise callback (" + trigger.getGroup() + ", " + trigger.getName() + ")", e);
                }
            }
        }
        finally
        {
            started = true;
        }
    }

    public Trigger getTrigger(String name, String group)
    {
        return triggerDao.findByNameAndGroup(name, group);
    }

    public Trigger getTrigger(long id)
    {
        return triggerDao.findById(id);
    }

    public List<Trigger> getTriggers()
    {
        return triggerDao.findAll();
    }

    public List<Trigger> getTriggers(long project)
    {
        return triggerDao.findByProject(project);
    }

    public Trigger getTrigger(long project, String triggerName)
    {
        return triggerDao.findByProjectAndName(project, triggerName);
    }

    public void schedule(Trigger trigger) throws SchedulingException
    {
        if (!trigger.isPersistent())
        {
            Trigger existingTrigger = triggerDao.findByNameAndGroup(trigger.getName(), trigger.getGroup());
            if (existingTrigger != null)
            {
                throw new SchedulingException("A trigger with name " + trigger.getName() + " and group " + trigger.getGroup() + " has already been scheduled.");
            }
        }

        if (trigger.isScheduled())
        {
            throw new SchedulingException("Trigger is already scheduled.");
        }

        trigger.setState(TriggerState.SCHEDULED);
        triggerDao.save(trigger);

        try
        {
            if (started)
            {
                SchedulerStrategy impl = getStrategy(trigger);
                if (impl == null)
                {
                    throw new SchedulingException("No strategy associated with trigger of type " + trigger.getType());
                }
                impl.schedule(trigger);
            }
        }
        catch (SchedulingException e)
        {
            triggerDao.delete(trigger);
            throw e;
        }
    }

    public void unschedule(final Trigger trigger) throws SchedulingException
    {
        assertScheduled(trigger);

        trigger.setState(TriggerState.NONE);
        triggerDao.save(trigger);

        SchedulerStrategy impl = getStrategy(trigger);
        impl.unschedule(trigger);

        triggerDao.delete(trigger);
    }

    public void update(Trigger trigger) throws SchedulingException
    {
        assertScheduled(trigger);

        triggerDao.save(trigger);

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
        for (Trigger trigger : triggerDao.findByGroup(group))
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
        triggerDao.save(trigger);

        SchedulerStrategy impl = getStrategy(trigger);
        impl.pause(trigger);
    }

    public void resume(String group) throws SchedulingException
    {
        for (Trigger trigger : triggerDao.findByGroup(group))
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
        triggerDao.save(trigger);

        SchedulerStrategy impl = getStrategy(trigger);
        impl.resume(trigger);
    }

    public void registerCallback(NullaryProcedure callback, Date when) throws SchedulingException
    {
        SimpleTrigger trigger = new SimpleTrigger(String.valueOf(nextCallbackId++), CALLBACK_TRIGGER_GROUP, when);
        trigger.setTaskClass(CallbackTask.class);

        registerCallbackTrigger(callback, trigger);
    }

    public void registerCallback(NullaryProcedure callback, long interval) throws SchedulingException
    {
        SimpleTrigger trigger = new SimpleTrigger(String.valueOf(nextCallbackId++), CALLBACK_TRIGGER_GROUP, interval);
        trigger.setTaskClass(CallbackTask.class);

        registerCallbackTrigger(callback, trigger);
    }

    private void registerCallbackTrigger(NullaryProcedure callback, Trigger trigger) throws SchedulingException
    {
        SchedulerStrategy impl = getStrategy(trigger);
        if (impl == null)
        {
            throw new SchedulingException("No strategy associated with trigger of type " + trigger.getType());
        }

        Pair<NullaryProcedure, Trigger> value = asPair(callback, trigger);
        if (started)
        {
            registeredCallbacks.put(trigger.getName(), value);
            impl.schedule(trigger);
        }
        else
        {
            // defer the scheduling of this trigger till we start the scheduler.
            callbacksToRegister.add(value);
        }
    }

    public NullaryProcedure getCallback(String name)
    {
        if (registeredCallbacks.containsKey(name))
        {
            return registeredCallbacks.get(name).getFirst();
        }
        return null;
    }

    public boolean unregisterCallback(NullaryProcedure callback) throws SchedulingException
    {
        if (callback == null)
        {
            return false;
        }
        
        HasCallbackPredicate hasCallbackPredicate = new HasCallbackPredicate(callback);
        Pair<NullaryProcedure, Trigger> found = CollectionUtils.find(registeredCallbacks.values(), hasCallbackPredicate);
        if (found == null)
        {
            found = CollectionUtils.find(callbacksToRegister, hasCallbackPredicate);
        }
        if (found == null)
        {
            return false;
        }

        Trigger trigger = found.getSecond();
        getStrategy(trigger).unschedule(trigger);

        registeredCallbacks.remove(trigger.getName());
        callbacksToRegister.remove(found);

        return true;
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
        if (!trigger.isScheduled())
        {
            throw new SchedulingException("The trigger must be scheduled.");
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

    public void setTriggerHandler(TriggerHandler triggerHandler)
    {
        this.triggerHandler = triggerHandler;
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

    private static class HasCallbackPredicate implements Predicate<Pair<NullaryProcedure, Trigger>>
    {
        private NullaryProcedure callback;

        private HasCallbackPredicate(NullaryProcedure callback)
        {
            this.callback = callback;
        }

        public boolean satisfied(Pair<NullaryProcedure, Trigger> pair)
        {
            return pair.getFirst() == callback;
        }
    }

    public static class CallbackTask implements Task
    {
        private Scheduler scheduler;

        public void execute(TaskExecutionContext context)
        {
            NullaryProcedure callback = scheduler.getCallback(context.getTrigger().getName());
            if (callback != null)
            {
                callback.process();
            }
        }

        public void setScheduler(Scheduler scheduler)
        {
            this.scheduler = scheduler;
        }
    }
}
