package com.zutubi.pulse.prototype.config.project.triggers;

import com.zutubi.prototype.config.*;
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
public class TriggerManager implements ExternalStateManager<TriggerConfiguration>
{
    private static final Logger LOG = Logger.getLogger(TriggerManager.class);

    private Scheduler scheduler;

    private ConfigurationProvider configurationProvider;

    public void init()
    {
        TypeListener<TriggerConfiguration> listener = new TypeAdapter<TriggerConfiguration>(TriggerConfiguration.class)
        {
            public void postDelete(TriggerConfiguration instance)
            {
                TriggerManager.this.delete(instance.getTriggerId());
            }

            public void postSave(TriggerConfiguration instance, boolean nested)
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
        listener.register(configurationProvider, true);
    }

    private void delete(long id)
    {
        try
        {
            Trigger trigger = scheduler.getTrigger(id);
            scheduler.unschedule(trigger);
        }
        catch (SchedulingException e)
        {
            LOG.severe(e);
        }
    }

    public long createState(TriggerConfiguration instance)
    {
        try
        {
            ComponentContext.autowire(instance);
            Trigger trigger = instance.newTrigger();
            scheduler.schedule(trigger);
            return trigger.getId();
        }
        catch (SchedulingException e)
        {
            LOG.severe(e);
            throw new PulseRuntimeException(e);
        }
    }

    public void rollbackState(long id)
    {
        delete(id);
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setConfigurationStateManager(ConfigurationStateManager configurationStateManager)
    {
        configurationStateManager.register(TriggerConfiguration.class, this);
    }
}
