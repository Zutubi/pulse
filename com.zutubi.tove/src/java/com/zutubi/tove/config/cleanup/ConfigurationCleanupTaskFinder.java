package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.ReflectionUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

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
            taskListingMethod = CollectionUtils.find(cleanupTasksClass.getMethods(), new Predicate<Method>()
            {
                public boolean satisfied(Method method)
                {
                    return method.getName().equals("getTasks") &&
                           (ReflectionUtils.acceptsParameters(method) || ReflectionUtils.acceptsParameters(method, configurationClass)) &&
                           ReflectionUtils.returnsParameterisedType(method, List.class, RecordCleanupTask.class);
                }
            });

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
