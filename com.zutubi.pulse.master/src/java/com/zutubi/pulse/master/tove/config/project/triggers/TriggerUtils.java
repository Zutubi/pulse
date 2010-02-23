package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.EXTENSION_PROJECT_TRIGGERS;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.InstanceOfPredicate;

import java.util.Map;
import java.util.List;
import java.util.LinkedList;

public class TriggerUtils
{
    /**
     * Get a trigger from the project configuration that is of the specified type.  If more than one such trigger
     * is present, this method returns the first trigger found.
     *
     * @param projectConfig the project configuration from which the trigger is being retrieved.
     * @param type          the type of the trigger being retrieved.
     * @param <T>           an extension of TriggerConfiguration.
     * @return a trigger instance of the specified type, or null if non exists.
     */
    public static <T extends TriggerConfiguration> T getTrigger(ProjectConfiguration projectConfig, Class<T> type)
    {
        Map<String, TriggerConfiguration> triggers = (Map<String, TriggerConfiguration>) projectConfig.getExtensions().get(EXTENSION_PROJECT_TRIGGERS);
        if (triggers != null)
        {
            return (T) CollectionUtils.find(triggers.values(), new InstanceOfPredicate<TriggerConfiguration>(type));
        }
        return null;
    }

    /**
     * Get the triggers from the project configuration that are of the specified type.
     *
     * @param projectConfig the project configuration from which the triggers are being retrieved.
     * @param type          the type of the triggers being retrieved.
     * @param <T>           an extension of TriggerConfiguration.
     * @return a list of triggers of the specified type, or an empty list of no triggers of the
     * specified type exist.
     */
    public static <T extends TriggerConfiguration> List<T> getTriggers(ProjectConfiguration projectConfig, Class<T> type)
    {
        Map<String, TriggerConfiguration> triggers = (Map<String, TriggerConfiguration>) projectConfig.getExtensions().get(EXTENSION_PROJECT_TRIGGERS);
        if (triggers != null)
        {
            return (List<T>) CollectionUtils.filter(triggers.values(), new InstanceOfPredicate<TriggerConfiguration>(type));
        }
        return new LinkedList<T>();
    }
}
