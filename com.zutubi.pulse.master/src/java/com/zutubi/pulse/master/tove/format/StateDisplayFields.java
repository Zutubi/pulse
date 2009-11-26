package com.zutubi.pulse.master.tove.format;

import com.zutubi.i18n.Messages;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import static com.zutubi.util.reflection.MethodPredicates.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Describes all available state display fields for a configuration type.
 */
public class StateDisplayFields
{
    private static final Logger LOG = Logger.getLogger(StateDisplayFields.class);

    private static final String METHOD_LIST_FIELDS = "getFields";
    private static final String METHOD_LIST_COLLECTION_FIELDS = "getCollectionFields";
    private static final String METHOD_PREFIX_FORMAT = "format";
    private static final String METHOD_PREFIX_FORMAT_COLLECTION = "formatCollection";

    private Class configurationClass;
    private Class<?> stateDisplayClass;
    private Object stateDisplayInstance;
    private Method fieldListingMethod;
    private Map<String, Method> availableFields = new HashMap<String, Method>();
    private Method collectionFieldListingMethod;
    private Map<String, Method> availableCollectionFields = new HashMap<String, Method>();

    public StateDisplayFields(Class configurationClass, Class<?> stateDisplayClass, ObjectFactory objectFactory)
    {
        this.configurationClass = configurationClass;
        this.stateDisplayClass = stateDisplayClass;
        if (stateDisplayClass != null)
        {
            createDisplayInstance(configurationClass, stateDisplayClass, objectFactory);
            findFieldListingMethod();
            findAvailableFields();
        }
    }

    private void createDisplayInstance(Class configurationClass, Class<?> stateDisplayClass, ObjectFactory objectFactory)
    {
        stateDisplayInstance = objectFactory.buildBean(stateDisplayClass);
        if (stateDisplayInstance instanceof MessagesAware)
        {
            ((MessagesAware) stateDisplayInstance).setMessages(Messages.getInstance(configurationClass));
        }
    }

    private void findFieldListingMethod()
    {
        Method[] methods = stateDisplayClass.getMethods();
        fieldListingMethod = CollectionUtils.find(methods,
                and(hasName(METHOD_LIST_FIELDS), or(acceptsParameters(), acceptsParameters(configurationClass)), returnsType(List.class, String.class)));
        collectionFieldListingMethod = CollectionUtils.find(methods,
                and(
                        hasName(METHOD_LIST_COLLECTION_FIELDS),
                        or(
                                acceptsParameters(),
                                acceptsParameters(Collection.class),
                                acceptsParameters(Collection.class, Configuration.class)
                        ),
                        returnsType(List.class, String.class)
                )
        );
    }

    private void findAvailableFields()
    {
        Method[] methods = stateDisplayClass.getMethods();
        for (Method method : methods)
        {
            String methodName = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            int parameterCount = parameterTypes.length;

            boolean isCollection;
            if (isCollectionFormatMethodName(methodName))
            {
                isCollection = true;
                if (parameterCount > 2)
                {
                    continue;
                }

                if (parameterCount > 1 && !parameterTypes[1].isAssignableFrom(Configuration.class))
                {
                    continue;
                }

                if (parameterCount > 0 && !parameterTypes[0].isAssignableFrom(Collection.class))
                {
                    continue;
                }
            }
            else if (isFormatMethodName(methodName))
            {
                isCollection = false;
                if (parameterCount > 1)
                {
                    continue;
                }

                if (parameterCount > 0 && !parameterTypes[0].isAssignableFrom(configurationClass))
                {
                    continue;
                }
            }
            else
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

            // ok, we have a field here.
            if (isCollection)
            {
                availableCollectionFields.put(stripPrefix(methodName, METHOD_PREFIX_FORMAT_COLLECTION), method);
            }
            else
            {
                availableFields.put(stripPrefix(methodName, METHOD_PREFIX_FORMAT), method);
            }
        }
    }

    private boolean isFormatMethodName(String methodName)
    {
        return isPrefixedMethodName(methodName, METHOD_PREFIX_FORMAT);
    }

    private boolean isCollectionFormatMethodName(String methodName)
    {
        return isPrefixedMethodName(methodName, METHOD_PREFIX_FORMAT_COLLECTION);
    }

    private boolean isPrefixedMethodName(String methodName, String prefix)
    {
        return methodName.startsWith(prefix) && methodName.length() > prefix.length();
    }

    private String stripPrefix(String methodName, String prefix)
    {
        int prefixLength = prefix.length();
        String fieldName = methodName.substring(prefixLength, prefixLength + 1).toLowerCase();
        if (methodName.length() > prefixLength + 1)
        {
            fieldName = fieldName + methodName.substring(prefixLength + 1);
        }
        return fieldName;
    }

    public Class getConfigurationClass()
    {
        return configurationClass;
    }

    public Class getStateDisplayClass()
    {
        return stateDisplayClass;
    }

    public boolean hasField(String name)
    {
        return getFieldMethod(name) != null;
    }

    public boolean hasCollectionField(String name)
    {
        return getCollectionFieldMethod(name) != null;
    }

    private Method getFieldMethod(String name)
    {
        return availableFields.get(name);
    }

    private Method getCollectionFieldMethod(String name)
    {
        return availableCollectionFields.get(name);
    }

    List<String> getFields(Configuration configurationInstance) throws Exception
    {
        return internalGetFields(availableFields, fieldListingMethod, configurationInstance);
    }

    List<String> getCollectionFields(Collection<? extends Configuration> configurationInstances, Configuration parentInstance) throws Exception
    {
        return internalGetFields(availableCollectionFields, collectionFieldListingMethod, configurationInstances, parentInstance);
    }

    private List<String> internalGetFields(Map<String, Method> allFields, Method listingMethod, Object... listingArgs) throws IllegalAccessException, InvocationTargetException
    {
        List<String> fields;
        if (listingMethod == null)
        {
            fields = new LinkedList<String>(allFields.keySet());
        }
        else
        {
            @SuppressWarnings({"unchecked"})
            List<String> fieldNames = (List<String>) invokeMethod(listingMethod, listingArgs);
            fields = new LinkedList<String>();
            for (String fieldName: fieldNames)
            {
                if (allFields.containsKey(fieldName))
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
        return invokeMethod(getFieldMethod(fieldName), configurationInstance);
    }

    public Object formatCollection(String fieldName, Collection<? extends Configuration> collection, Configuration parentInstance) throws Exception
    {
        return invokeMethod(getCollectionFieldMethod(fieldName), collection, parentInstance);
    }

    public Object invokeMethod(Method method, Object... parameters) throws InvocationTargetException, IllegalAccessException
    {
        int parameterCount = method.getParameterTypes().length;
        if (parameterCount < parameters.length)
        {
            Object[] actualParameters = new Object[parameterCount];
            System.arraycopy(parameters, 0, actualParameters, 0, parameterCount);
            parameters = actualParameters;
        }

        return method.invoke(stateDisplayInstance, parameters);
    }
}
