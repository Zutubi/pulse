package com.zutubi.pulse.master.tove.table;

import com.zutubi.tove.annotations.Format;
import com.zutubi.pulse.master.tove.table.ColumnFormatter;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.bean.ObjectFactory;

import java.lang.reflect.Method;

/**
 * A wrapper object that provides access to formatted property values via the #FormattingWrapper.get(String name) method.
 *
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
    }

    public Object get(String name) throws Exception
    {
        // type class level formatting
        try
        {
            // very inefficient. should record the class level formatter class somewhere.

            Class<Object> formatter = ConventionSupport.getFormatter(type);
            if (formatter != null)
            {
                Object formatterInstance = objectFactory.buildBean(formatter);

                String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
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

                // handle the case when the accepted arg is of the base type.
                try
                {
                    getter = formatter.getMethod(methodName, type.getClazz());
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
            // noop.
        }

        // column level formatting
        TypeProperty property = type.getProperty(name);
        if (property != null)
        {
            Format columnFormatter = property.getAnnotation(Format.class);
            if (columnFormatter != null)
            {
                Class formatter = ClassLoaderUtils.loadAssociatedClass(type.getClazz(), columnFormatter.value());
                ColumnFormatter formatterInstance = (ColumnFormatter) formatter.newInstance();
                Object fieldValue = getFieldValue(name);
                return formatterInstance.format(fieldValue);
            }
        }
        return getFieldValue(name);
    }

    private Object getFieldValue(String name) throws Exception
    {
        TypeProperty property = type.getProperty(name);
        if (property != null)
        {
            return property.getValue(instance);
        }

        // default method invocation.
        try
        {
            Method getter = instance.getClass().getMethod("get" + name.substring(0,1).toUpperCase() + name.substring(1));
            if (getter != null)
            {
                return getter.invoke(instance);
            }
        }
        catch (Exception e)
        {
            // noop.
        }
        return null;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
