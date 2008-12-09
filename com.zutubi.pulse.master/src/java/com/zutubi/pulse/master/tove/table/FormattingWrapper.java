package com.zutubi.pulse.master.tove.table;

import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.annotations.Format;
import com.zutubi.tove.annotations.Formatter;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.bean.BeanUtils;

import java.lang.reflect.Method;

/**
 * A wrapper object that provides access to formatted property values.
 */
public class FormattingWrapper
{
    private ObjectFactory objectFactory;

    /**
     * The instance being wrapped.
     */
    private Object instance;

    /**
     * The type definition for the wrapped instance.
     */
    private CompositeType type;

    public FormattingWrapper(Object instance, CompositeType type)
    {
        this.instance = instance;
        this.type = type;

        if (instance.getClass() != type.getClazz())
        {
            throw new IllegalArgumentException("Instance: " + instance + " not of the expected type: " + type.getClazz());
        }
    }

    @SuppressWarnings({"unchecked"})
    public Object get(String name) throws Exception
    {
        try
        {
            Class<Object> formatter = ConventionSupport.getFormatter(type);
            if (formatter != null)
            {
                Object formatterInstance = objectFactory.buildBean(formatter);

                String methodName = getGetterMethodName(name);
                Method getter;
                try
                {
                    getter = formatter.getMethod(methodName, instance.getClass());
                    if (getter != null)
                    {
                        return getter.invoke(formatterInstance, instance);
                    }
                }
                catch (NoSuchMethodException e)
                {
                    // noop
                }
            }
        }
        catch (Exception e)
        {
            // should we be warning about this exception? if so, how/where is most appropriate.
        }

        Object fieldValue = getFieldValue(name);

        TypeProperty property = type.getProperty(name);
        if (property != null)
        {
            Format formatAnnotation = property.getAnnotation(Format.class);
            if (formatAnnotation != null)
            {
                Class clazz = ClassLoaderUtils.loadAssociatedClass(type.getClazz(), formatAnnotation.value());
                if (Formatter.class.isAssignableFrom(clazz))
                {
                    Formatter formatterInstance = (Formatter) objectFactory.buildBean(clazz);
                    return formatterInstance.format(fieldValue);
                }
                else
                {
                    // should we be warning about this? if so, how/where is most appropriate.
                }
            }
        }
        
        return fieldValue;
    }

    private Object getFieldValue(String name) throws Exception
    {
        TypeProperty property = type.getProperty(name);
        if (property != null)
        {
            return property.getValue(instance);
        }
        else
        {
            // should we ever end up here?. Any requested property will surely
            // refer to a defined property.
            try
            {
                return BeanUtils.getProperty(name, instance);
            }
            catch (Exception e)
            {
                // should we be warning about this exception? if so, how/where is most appropriate.
            }
            return null;
        }
    }

    private String getGetterMethodName(String name)
    {
        return "get" + name.substring(0,1).toUpperCase() + name.substring(1);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
