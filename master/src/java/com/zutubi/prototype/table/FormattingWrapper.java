package com.zutubi.prototype.table;

import com.zutubi.config.annotations.Format;
import com.zutubi.prototype.ColumnFormatter;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.pulse.bootstrap.ComponentContext;

import java.lang.reflect.Method;

/**
 * A wrapper object that provides access to formatted property values via the #FormattingWrapper.get(String name) method.
 *
 */
public class FormattingWrapper
{
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
            
            String formatterName = type.getClazz().getName() + "Formatter";
            Class<Object> formatter = ClassLoaderUtils.loadAssociatedClass(type.getClazz(), formatterName);
            
            // FIXME: maybe this should be calling the object factory instead.
            Object formatterInstance = ComponentContext.createBean(formatter);

            String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
            Method getter = formatter.getMethod(methodName, instance.getClass());
            if (getter != null)
            {
                return getter.invoke(formatterInstance, instance);
            }
        }
        catch (Exception e)
        {
//                e.printStackTrace();
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
//            e.printStackTrace();
        }
        return null;
    }
}
