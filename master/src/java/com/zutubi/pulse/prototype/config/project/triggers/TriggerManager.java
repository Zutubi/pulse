package com.zutubi.pulse.prototype.config.project.triggers;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.TypeListener;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.PulseRuntimeException;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.util.logging.Logger;

/**
 * 
 *
 */
public class TriggerManager
{
    private static final Logger LOG = Logger.getLogger(TriggerManager.class);

    private Scheduler scheduler;

    private ConfigurationProvider configurationProvider;

    public void init()
    {
        TypeListener<TriggerConfiguration> listener = new TypeListener<TriggerConfiguration>(TriggerConfiguration.class)
        {
            public void postInsert(TriggerConfiguration instance)
            {
                try
                {
                    ComponentContext.autowire(instance);
                    Trigger trigger = instance.newTrigger();
                    scheduler.schedule(trigger);
                    instance.setTriggerId(trigger.getId());

                    // resave required 
                    configurationProvider.save(instance);
                }
                catch (SchedulingException e)
                {
                    LOG.severe(e);
                    throw new PulseRuntimeException(e);
                }
            }

            public void postDelete(TriggerConfiguration instance)
            {
                try
                {
                    Trigger trigger = scheduler.getTrigger(instance.getTriggerId());
                    scheduler.unschedule(trigger);
                }
                catch (SchedulingException e)
                {
                    LOG.severe(e);
                }
            }

            public void postSave(TriggerConfiguration instance)
            {
                try
                {
                    Trigger trigger = scheduler.getTrigger(instance.getTriggerId());
                    instance.update(trigger);

                    // on an update, the details of the trigger will have changed.  We need to update the
                    // scheduler accordingly.
                    scheduler.update(trigger);
                }
                catch (SchedulingException e)
                {
                    LOG.severe(e);
                }
            }
        };
        listener.register(configurationProvider);
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
