package com.zutubi.tove.ui.format;

import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.EnumType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.util.EnumUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.reflection.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates formatted properties for configuration instances.  These are produced by *Formatter
 * classes alongside a configuration class.  Each method in the formatter class with a name get*
 * that takes an instance of the config type as its only argument and returns a result produces a
 * property, which may be a human-friendly version of a persistent property, or an additional
 * property only used in UI (maybe, e.g. a summary that is a comnbination of simple values).
 * <p/>
 * In addition to explicitly-formatted properties:
 * <ul>
 *  <li>@{@link Table} annotations may reference transient properties in composites, which will
 *      be included with default formatting (if not rendered by a Formatter).</li>
 *  <li>Default human-friendly formatting of persistent properties is produced where possible, e.g
 *      for enum values (we convert to the pretty string equivalent).</li>
 * </ul>
 */
public class ConfigPropertyFormatter
{
    private static final Logger LOG = Logger.getLogger(ConfigPropertyFormatter.class);

    private Map<String, TypeFormatter> formattersBySymbolicName = new HashMap<>();
    private ObjectFactory objectFactory;

    public Map<String, Object> getFormattedProperties(Configuration instance, CompositeType type) throws TypeException
    {
        TypeFormatter typeFormatter = formattersBySymbolicName.get(type.getSymbolicName());
        if (typeFormatter == null)
        {
            typeFormatter = createTypeFormatter(type);
            formattersBySymbolicName.put(type.getSymbolicName(), typeFormatter);
        }

        Map<String, Object> properties = new HashMap<>();
        if (typeFormatter.formatterInstance != null)
        {
            for (PropertyFormatter pf: typeFormatter.formattedProperties)
            {
                try
                {
                    Object result = pf.method.invoke(typeFormatter.formatterInstance, instance);
                    if (result != null)
                    {
                        properties.put(pf.name, result);
                    }
                }
                catch (Exception e)
                {
                    LOG.severe(e);
                }

            }
        }

        for (PropertyFormatter pf: typeFormatter.extraProperties)
        {
            try
            {
                Object result = pf.method.invoke(instance);
                if (result != null)
                {
                    if (result instanceof Enum)
                    {
                        result = EnumUtils.toPrettyString((Enum) result);
                    }
                    properties.put(pf.name, result);
                }
            }
            catch (InvocationTargetException | IllegalAccessException e)
            {
                // noop
            }

        }

        return properties;
    }

    private TypeFormatter createTypeFormatter(CompositeType type)
    {
        TypeFormatter typeFormatter = new TypeFormatter();

        Class<?> formatter = ConventionSupport.loadClass(type, "Formatter", Object.class);
        if (formatter != null)
        {
            typeFormatter.formatterInstance = objectFactory.buildBean(formatter);
            for (Method method : formatter.getMethods())
            {
                if (isGetter(method, type))
                {
                    typeFormatter.formattedProperties.add(new PropertyFormatter(method));
                }
            }
        }

        // If there is a table annotation, it could reference transient properties which we
        // include with default formatting.
        Table table = type.getAnnotation(Table.class, true);
        if (table != null)
        {
            for (String column: table.columns())
            {
                // We ignore properties which are already in the formatted or default list, taking care around
                // enums which are formatted by default (so the existing property value can't be directly used).
                if (!typeFormatter.hasFormattedProperty(column) && (!type.hasProperty(column) || type.getPropertyType(column) instanceof EnumType))
                {
                    String methodName = getGetterMethodName(column);
                    try
                    {
                        Method getter = type.getClazz().getMethod(methodName);
                        typeFormatter.extraProperties.add(new PropertyFormatter(column, getter));
                    }
                    catch (NoSuchMethodException e)
                    {
                        // noop
                    }
                }
            }
        }

        return typeFormatter;
    }

    private boolean isGetter(Method method, CompositeType type)
    {
        return method.getName().length() > 3 &&
                method.getName().startsWith("get") &&
                ReflectionUtils.acceptsParameters(method, type.getClazz()) &&
                ReflectionUtils.returnsType(method, Object.class);
    }

    private String getGetterMethodName(String name)
    {
        return "get" + name.substring(0,1).toUpperCase() + name.substring(1);
    }

    private static class TypeFormatter
    {
        private Object formatterInstance;
        private List<PropertyFormatter> formattedProperties = new ArrayList<>();
        private List<PropertyFormatter> extraProperties = new ArrayList<>();

        private boolean hasFormattedProperty(String name)
        {
            for (PropertyFormatter pf: formattedProperties)
            {
                if (pf.name.equals(name))
                {
                    return true;
                }
            }

            return false;
        }
    }

    private static class PropertyFormatter
    {
        private String name;
        private Method method;

        private PropertyFormatter(Method method)
        {
            this(method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4), method);
        }

        private PropertyFormatter(String name, Method method)
        {
            this.name = name;
            this.method = method;
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
