package com.zutubi.pulse.servercore.agent;

import com.zutubi.tove.squeezer.Squeezers;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.reflection.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

/**
 * Support base for implementing synchronisation tasks.  Implements conversion
 * to and from {@link com.zutubi.pulse.servercore.agent.SynchronisationMessage}
 * instances.  Task fields are bound to the arguments of the message.
 * <p/>
 * Subclasses should:
 *
 * <ul>
 *   <li>Declare all arguments required for the task as fields.  These will be
 *       handled by this implementation.</li>
 *   <li>Use only field types that have corresponding {@link com.zutubi.tove.squeezer.Squeezers}.</li>
 *   <li>Mark any fields that should not be sent in messages as transient.
 *       (Note that static and final fields are also ignored during binding.)</li>
 *   <li>Include a constructor that takes a single {@link java.util.Properties}
 *       argument and forwards to the corresponding constructor in this class.</li>
 *   <li>Avoid initialising fields, as this will overwrite changes made by the
 *       binding implementation.</li>
 * </ul>
 */
public abstract class SynchronisationTaskSupport implements SynchronisationTask
{
    protected SynchronisationTaskSupport()
    {
    }

    /**
     * Creates a new task from the given arguments.  The arguments are bound to
     * corresponding fields in the task class, where such fields exist.
     * Arguments with no corresponding field are ignored.
     *
     * @param arguments a mapping from field names to field values as strings
     */
    protected SynchronisationTaskSupport(Properties arguments)
    {
        try
        {
            for (Field field: getArgumentFields())
            {
                String value = (String) arguments.get(field.getName());
                if (value != null)
                {
                    TypeSqueezer squeezer = Squeezers.findSqueezer(field.getType());
                    ReflectionUtils.setFieldValue(this, field, squeezer.unsqueeze(value));
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public SynchronisationMessage toMessage()
    {
        try
        {
            Properties properties = new Properties();
            for (Field field: getArgumentFields())
            {
                field.setAccessible(true);
                Object value = field.get(this);
                if (value != null)
                {
                    TypeSqueezer squeezer = Squeezers.findSqueezer(field.getType());
                    properties.put(field.getName(), squeezer.squeeze(value));
                }
            }

            return new SynchronisationMessage(getType(), properties);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private Field[] getArgumentFields()
    {
        return CollectionUtils.filterToArray(getClass().getDeclaredFields(), new Predicate<Field>()
        {
            public boolean satisfied(Field field)
            {
                int modifiers = field.getModifiers();
                return !Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers);
            }
        });
    }
}
