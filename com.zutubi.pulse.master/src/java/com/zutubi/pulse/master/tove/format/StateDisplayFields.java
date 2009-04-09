package com.zutubi.pulse.master.tove.format;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import static com.zutubi.util.reflection.MethodPredicates.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Describes all available state display fields for a configuration type.
 */
public class StateDisplayFields
{
    private static final Logger LOG = Logger.getLogger(StateDisplayFields.class);

    private Class configurationClass;
    private Class stateDisplayClass;
    private Method fieldListingMethod;
    private Map<String, Method> availableFields = new HashMap<String, Method>();
    private ObjectFactory objectFactory;

    public StateDisplayFields(Class configurationClass, Class stateDisplayClass, ObjectFactory objectFactory)
    {
        this.configurationClass = configurationClass;
        this.stateDisplayClass = stateDisplayClass;
        this.objectFactory = objectFactory;
        findFieldListingMethod();
        findAvailableFields();
    }

    private void findFieldListingMethod()
    {
        if (stateDisplayClass != null)
        {
            fieldListingMethod = CollectionUtils.find(stateDisplayClass.getMethods(),
                    and(hasName("getFields"), or(acceptsParameters(), acceptsParameters(configurationClass)), returnsType(List.class, String.class)));
        }
    }

    private void findAvailableFields()
    {
        if(stateDisplayClass != null)
        {
            Method[] methods = stateDisplayClass.getMethods();
            for (Method method : methods)
            {
                String methodName = method.getName();
                if (!methodName.startsWith("format") || methodName.length() == 6)
                {
                    continue;
                }
                if (method.getDeclaringClass() == Object.class)
                {
                    continue;
                }
                if (method.getReturnType() == Void.TYPE)
                {
                    continue;
                }

                int parameterCount = method.getParameterTypes().length;
                if (parameterCount > 1)
                {
                    continue;
                }

                if (parameterCount > 0)
                {
                    Class param = method.getParameterTypes()[0];
                    if (!param.isAssignableFrom(configurationClass))
                    {
                        continue;
                    }
                }

                // ok, we have a field here.
                String fieldName = methodName.substring(6, 7).toLowerCase();
                if (methodName.length() > 7)
                {
                    fieldName = fieldName + methodName.substring(7);
                }
                availableFields.put(fieldName, method);
            }
        }
    }

    public Class getConfigurationClass()
    {
        return configurationClass;
    }

    public Class getStateDisplayClass()
    {
        return stateDisplayClass;
    }

    public Method getFieldMethod(String name)
    {
        return availableFields.get(name);
    }

    public Iterable<String> getAvailableFields()
    {
        return availableFields.keySet();
    }

    public boolean hasField(String name)
    {
        return getFieldMethod(name) != null;
    }

    List<String> getFields(Object configurationInstance) throws Exception
    {
        List<String> fields;
        if (fieldListingMethod == null)
        {
            fields = new LinkedList<String>(availableFields.keySet());
        }
        else
        {
            List<String> fieldNames;

            Object stateDisplay = objectFactory.buildBean(stateDisplayClass);
            if (fieldListingMethod.getParameterTypes().length == 0)
            {
                fieldNames = (List<String>) fieldListingMethod.invoke(stateDisplay);
            }
            else
            {
                fieldNames = (List<String>) fieldListingMethod.invoke(stateDisplay, configurationInstance);
            }

            fields = new LinkedList<String>();
            for(String fieldName: fieldNames)
            {
                if(hasField(fieldName))
                {
                    fields.add(fieldName);
                }
                else
                {
                    LOG.warning("Dropping state display field '" + fieldName + "' from class '" + configurationClass.getName() + "' because no corresponding method was found");
                }
            }
        }

        return fields;
    }

    public Object format(String fieldName, Configuration configurationInstance) throws Exception
    {
        Object displayInstance = objectFactory.buildBean(stateDisplayClass);
        Method method = getFieldMethod(fieldName);
        if(method.getParameterTypes().length == 0)
        {
            return method.invoke(displayInstance);
        }
        else
        {
            return method.invoke(displayInstance, configurationInstance);
        }
    }
}
