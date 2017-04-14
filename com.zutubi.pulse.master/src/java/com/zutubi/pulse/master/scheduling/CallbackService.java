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

package com.zutubi.pulse.master.scheduling;

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.find;
import com.zutubi.i18n.Messages;
import com.zutubi.util.logging.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
    private Map<String, Runnable> registeredCallbacks = new HashMap<String, Runnable>();

    /**
     * Register the procedure to be called back at the specified time.
     *
     * @param callback  the procedure to be called
     * @param when      the time at which the procedure should be called.
     */
    public synchronized void registerCallback(Runnable callback, Date when)
    {
        registerCallback(String.valueOf(nextCallbackId.getAndIncrement()), callback, when);
    }

    /**
     * Register the procedure to be called back at a specified time.
     *
     * @param name      the name to uniquely identify the procedure.
     * @param callback  the procedure to be called.
     * @param when      the time at which the procedure will be called.
     */
    public synchronized void registerCallback(String name, Runnable callback, Date when)
    {
        assertNotRegistered(name, callback);

        SimpleTrigger trigger = new SimpleTrigger(name, CALLBACK_TRIGGER_GROUP, when);
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
    public synchronized void registerCallback(Runnable callback, long interval)
    {
        registerCallback(String.valueOf(nextCallbackId.getAndIncrement()), callback, interval);
    }

    /**
     * Register the procedure to be called back at the defined intervals.
     *
     * @param name      the name to uniquely identify the procedure.
     * @param callback  the procedure to be called.
     * @param interval  the interval in milliseconds at which the callback is made.
     */
    public synchronized void registerCallback(String name, Runnable callback, long interval)
    {
        assertNotRegistered(name, callback);

        SimpleTrigger trigger = new SimpleTrigger(name, CALLBACK_TRIGGER_GROUP, interval);
        trigger.setTaskClass(CallbackTask.class);
        trigger.setTransient(true);
        registerCallbackTrigger(callback, trigger);
    }

    private void assertNotRegistered(String name, Runnable callback) throws IllegalArgumentException
    {
        if (registeredCallbacks.containsKey(name))
        {
            throw new IllegalArgumentException(I18N.format("callback.invalid.nameInUse", name));
        }
        if (isRegistered(callback))
        {
            throw new IllegalArgumentException(I18N.format("callback.invalid.alreadyRegistered"));
        }
    }

    private synchronized void registerCallbackTrigger(Runnable callback, Trigger trigger)
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
            throw new RuntimeException(I18N.format("schedule.failed", e.getMessage()));
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
    public synchronized boolean unregisterCallback(Runnable callback)
    {
        Map.Entry<String, Runnable> entry = find(registeredCallbacks.entrySet(), new ByCallbackPredicate(callback), null);
        if (entry != null)
        {
            String triggerName = entry.getKey();
            return unregisterCallback(triggerName);
        }
        return false;
    }

    /**
     * Stop the specified callback from being triggered.
     *
     * @param name  the name uniquely identifying the callback.
     *
     * @return true if the procedure was unregistered, false otherwise (for instance
     * if the procedure had not previously been registered)
     */
    public synchronized boolean unregisterCallback(String name)
    {
        try
        {
            if (registeredCallbacks.containsKey(name))
            {
                Trigger trigger = scheduler.getTrigger(name, CALLBACK_TRIGGER_GROUP);
                scheduler.unschedule(trigger);
                registeredCallbacks.remove(name);
                return true;
            }
            return false;
        }
        catch (SchedulingException e)
        {
            throw new RuntimeException(e);
        }
    }

    public synchronized Runnable getCallback(String name)
    {
        return registeredCallbacks.get(name);
    }

    private boolean isRegistered(Runnable callback)
    {
        return any(registeredCallbacks.entrySet(), new ByCallbackPredicate(callback));
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
            Runnable callback = service.getCallback(context.getTrigger().getName());
            if (callback != null)
            {
                try
                {
                    callback.run();
                }
                catch (Exception e)
                {
                    LOG.error(I18N.format("uncaught.exception", e.getMessage()), e);
                }
            }
        }

        public void setCallbackService(CallbackService callbackService)
        {
            this.service = callbackService;
        }
    }

    private static class ByCallbackPredicate implements Predicate<Map.Entry<String, Runnable>>
    {
        private Runnable callback;

        private ByCallbackPredicate(Runnable callback)
        {
            this.callback = callback;
        }

        public boolean apply(Map.Entry<String, Runnable> entry)
        {
            return entry.getValue() == callback;
        }
    }
}
