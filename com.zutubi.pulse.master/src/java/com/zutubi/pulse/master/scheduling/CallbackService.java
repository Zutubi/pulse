package com.zutubi.pulse.master.scheduling;

import com.zutubi.util.NullaryProcedure;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.logging.Logger;
import com.zutubi.i18n.Messages;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A service that allows clients to register themselves to receive callbacks
 * at specific intervals or times.
 * <p/>
 * The actual callbacks are implemented via transient triggers registered with
 * the scheduler service.
 * <p>
 * Note: An individual callback instance can only be registered once.  
 */
public class CallbackService
{
    private static final Messages I18N = Messages.getInstance(CallbackService.class);
    private static final Logger LOG = Logger.getLogger(CallbackService.class);

    /**
     * The trigger group name used for all triggers that the callback service
     * registers with the scheduler.
     */
    protected static final String CALLBACK_TRIGGER_GROUP = "callback-triggers";

    private Scheduler scheduler;

    /**
     * The next callback id is used to assign a new and unique name to each of the triggers
     * that we register with the scheduler.
     */
    private AtomicLong nextCallbackId = new AtomicLong(1);

    /**
     * The map of callback id to callback instance.
     */
    private Map<String, NullaryProcedure> registeredCallbacks = new HashMap<String, NullaryProcedure>();

    /**
     * Register the procedure to be called back at the specified time.
     *
     * @param callback  the procedure to be called
     * @param when      the time at which the procedure should be called.
     */
    public synchronized void registerCallback(NullaryProcedure callback, Date when)
    {
        assertNotRegistered(callback);

        SimpleTrigger trigger = new SimpleTrigger(String.valueOf(nextCallbackId.getAndIncrement()), CALLBACK_TRIGGER_GROUP, when);
        trigger.setTaskClass(CallbackTask.class);
        trigger.setTransient(true);
        registerCallbackTrigger(callback, trigger);
    }

    /**
     * Register the procedure to be called back at the defined intervals.
     *
     * @param callback  the procedure to be called.
     * @param interval  the interval in milliseconds at which the callback is made.
     */
    public synchronized void registerCallback(NullaryProcedure callback, long interval)
    {
        assertNotRegistered(callback);

        SimpleTrigger trigger = new SimpleTrigger(String.valueOf(nextCallbackId.getAndIncrement()), CALLBACK_TRIGGER_GROUP, interval);
        trigger.setTaskClass(CallbackTask.class);
        trigger.setTransient(true);
        registerCallbackTrigger(callback, trigger);
    }

    private void assertNotRegistered(NullaryProcedure callback) throws IllegalArgumentException
    {
        if (isRegistered(callback))
        {
            throw new IllegalArgumentException(I18N.format("callback.invalid.alreadyRegistered"));
        }
    }

    private synchronized void registerCallbackTrigger(NullaryProcedure callback, Trigger trigger)
    {
        try
        {
            scheduler.schedule(trigger);
            registeredCallbacks.put(trigger.getName(), callback);
        }
        catch (SchedulingException e)
        {
            // This error relates directly to a misconfiguration of the scheduler.  No point
            // trying to deal with it in the code.
            throw new RuntimeException("Failed to schedule callback trigger.");
        }
    }

    /**
     * Stop the specified callback from being triggered.
     *
     * @param callback  the previously registered callback procedure.
     *
     * @return true if the procedure was unregistered, false otherwise (for instance
     * if the procedure had not previously been registered) 
     */
    public synchronized boolean unregisterCallback(NullaryProcedure callback)
    {
        try
        {
            Map.Entry<String, NullaryProcedure> entry = CollectionUtils.find(registeredCallbacks.entrySet(), new ByCallbackPredicate(callback));
            if (entry != null)
            {
                String triggerName = entry.getKey();
                Trigger trigger = scheduler.getTrigger(triggerName, CALLBACK_TRIGGER_GROUP);
                scheduler.unschedule(trigger);
                registeredCallbacks.remove(triggerName);
                return true;
            }
            return false;
        }
        catch (SchedulingException e)
        {
            throw new RuntimeException(e);
        }
    }

    public synchronized NullaryProcedure getCallback(String name)
    {
        return registeredCallbacks.get(name);
    }

    private boolean isRegistered(NullaryProcedure callback)
    {
        return CollectionUtils.contains(registeredCallbacks.entrySet(), new ByCallbackPredicate(callback));
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    // needs to be public since it is instantiated by the scheduling system.
    public static class CallbackTask implements Task
    {
        private CallbackService service;

        public void execute(TaskExecutionContext context)
        {
            NullaryProcedure callback = service.getCallback(context.getTrigger().getName());
            if (callback != null)
            {
                try
                {
                    callback.run();
                }
                catch (Exception e)
                {
                    LOG.warning("Uncaught exception generated by callback: " + e.getMessage(), e);
                }
            }
        }

        public void setCallbackService(CallbackService callbackService)
        {
            this.service = callbackService;
        }
    }

    private static class ByCallbackPredicate implements Predicate<Map.Entry<String, NullaryProcedure>>
    {
        private NullaryProcedure callback;

        private ByCallbackPredicate(NullaryProcedure callback)
        {
            this.callback = callback;
        }

        public boolean satisfied(Map.Entry<String, NullaryProcedure> entry)
        {
            return entry.getValue() == callback;
        }
    }
}
