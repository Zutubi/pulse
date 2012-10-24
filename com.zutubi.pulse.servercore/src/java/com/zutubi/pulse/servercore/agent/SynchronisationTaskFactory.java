package com.zutubi.pulse.servercore.agent;

import com.zutubi.tove.squeezer.Squeezers;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.reflection.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Provides conversions between synchronisation tasks and messages.  The latter
 * are used for communication of tasks between master and agents, and for
 * persistence of the tasks.
 */
public class SynchronisationTaskFactory
{
    private static final Map<String, Class<? extends SynchronisationTask>> nameToClass = new HashMap<String, Class<? extends SynchronisationTask>>();
    private static final Map<Class<? extends SynchronisationTask>, String> classToName = new HashMap<Class<? extends SynchronisationTask>, String>();
    private ObjectFactory objectFactory;

    static
    {
        registerType(SynchronisationTask.Type.CLEANUP_DIRECTORY.name(), DeleteDirectoryTask.class);
        registerType(SynchronisationTask.Type.RENAME_DIRECTORY.name(), RenameDirectoryTask.class);
        registerType(SynchronisationTask.Type.TEST.name(), TestSynchronisationTask.class);
        registerType(SynchronisationTask.Type.TEST_ASYNC.name(), TestAsyncSynchronisationTask.class);
    }

    /**
     * Registers a mapping from a type name to the corresponding implementation task.
     *
     * @param name  name of the type (usually an enum name, see {@link com.zutubi.pulse.servercore.agent.SynchronisationTask.Type})
     * @param clazz the implementation class
     */
    public static void registerType(String name, Class<? extends SynchronisationTask> clazz)
    {
        nameToClass.put(name, clazz);
        classToName.put(clazz, name);
    }

    /**
     * Returns the task type name for tasks of the given type, if known.
     *
     * @param taskClass task type to get the name for
     * @return the type of the task, or null if the task class is not known
     */
    public static String getTaskType(Class<? extends SynchronisationTask> taskClass)
    {
        return classToName.get(taskClass);
    }

    /**
     * Creates a task from the given message.  The arguments in the message
     * are bound to corresponding fields in the task, where such fields exist.
     * Arguments with no corresponding field are ignored.
     *
     * @param message the message to create a task from
     * @return the created task
     */
    public SynchronisationTask fromMessage(SynchronisationMessage message)
    {
        Class<? extends SynchronisationTask> clazz = nameToClass.get(message.getTypeName());
        SynchronisationTask task = objectFactory.buildBean(clazz);
        Properties arguments = message.getArguments();
        try
        {
            for (Field field: getArgumentFields(clazz))
            {
                String value = (String) arguments.get(field.getName());
                if (value != null)
                {
                    TypeSqueezer squeezer = Squeezers.findSqueezer(field.getType());
                    ReflectionUtils.setFieldValue(task, field, squeezer.unsqueeze(value));
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return task;
    }

    /**
     * Converts the given task to a message which can be sent to an agent.  The
     * message can be converted back on the agent side using
     * {@link #fromMessage(SynchronisationMessage)}.
     *
     * @param task the task to convert
     * @return a message encoding the task and its arguments
     */
    public SynchronisationMessage toMessage(SynchronisationTask task)
    {
        try
        {
            Properties properties = new Properties();
            Class<? extends SynchronisationTask> taskClass = task.getClass();
            for (Field field: getArgumentFields(taskClass))
            {
                field.setAccessible(true);
                Object value = field.get(task);
                if (value != null)
                {
                    TypeSqueezer squeezer = Squeezers.findSqueezer(field.getType());
                    properties.put(field.getName(), squeezer.squeeze(value));
                }
            }

            return new SynchronisationMessage(getTaskType(taskClass), properties);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private Field[] getArgumentFields(Class taskClass)
    {
        return CollectionUtils.filterToArray(taskClass.getDeclaredFields(), new Predicate<Field>()
        {
            public boolean satisfied(Field field)
            {
                int modifiers = field.getModifiers();
                return !Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers);
            }
        });
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
