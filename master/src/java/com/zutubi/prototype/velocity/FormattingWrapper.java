package com.zutubi.prototype.velocity;

import com.zutubi.config.annotations.Format;
import com.zutubi.prototype.ColumnFormatter;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.util.ClassLoaderUtils;

import java.lang.reflect.Method;

/**
 *
 *
 */
public class FormattingWrapper
{
    private Object instance;
    private CompositeType type;

    public FormattingWrapper(Object instance, CompositeType type)
    {
        this.instance = instance;
        this.type = type;
    }

    public Object get(String name) throws Exception
    {
        // type class level formatting
        Format typeFormatter = (Format) type.getAnnotation(Format.class);
        if (typeFormatter != null)
        {
            try
            {
                Class formatter = ClassLoaderUtils.loadAssociatedClass(type.getClazz(), typeFormatter.value());
                Object formatterInstance = formatter.newInstance();
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
