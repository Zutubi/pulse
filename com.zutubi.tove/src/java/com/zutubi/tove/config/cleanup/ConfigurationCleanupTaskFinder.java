package com.zutubi.tove.config.cleanup;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Iterables.find;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import static com.zutubi.util.reflection.MethodPredicates.*;
import static java.util.Arrays.asList;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Used to find custom cleanup tasks that need to be executed when a record
 * is deleted.  Instances are cached in the
 * {@link ConfigurationCleanupManager} to avoid multiple calls to the
 * reflection APIs.
 */
public class ConfigurationCleanupTaskFinder
{
    private static final Logger LOG = Logger.getLogger(ConfigurationCleanupTaskFinder.class);

    private Class cleanupTasksClass;
    private ObjectFactory objectFactory;
    private Method taskListingMethod;

    public ConfigurationCleanupTaskFinder(final Class configurationClass, Class cleanupTasksClass, ObjectFactory objectFactory)
    {
        this.cleanupTasksClass = cleanupTasksClass;
        this.objectFactory = objectFactory;

        if (cleanupTasksClass != null)
        {
            taskListingMethod = find(asList(cleanupTasksClass.getMethods()),
                    and(hasName("getTasks"), or(acceptsParameters(), acceptsParameters(configurationClass)), returnsType(List.class, RecordCleanupTask.class)), null);

            if(taskListingMethod == null)
            {
                LOG.warning("No task listing method found in class '" + cleanupTasksClass.getName() + "'");
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<RecordCleanupTask> getCleanupTasks(Configuration instance) throws Exception
    {
        if(taskListingMethod == null)
        {
            return Collections.emptyList();
        }
        else
        {
            Object tasksInstance = objectFactory.<Object>buildBean(cleanupTasksClass);
            if(taskListingMethod.getParameterTypes().length == 0)
            {
                return (List<RecordCleanupTask>) taskListingMethod.invoke(tasksInstance);
            }
            else
            {
                return (List<RecordCleanupTask>) taskListingMethod.invoke(tasksInstance, instance);
            }
        }
    }
}
