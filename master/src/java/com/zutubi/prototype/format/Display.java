package com.zutubi.prototype.format;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.ReflectionUtils;
import com.zutubi.util.bean.ObjectFactory;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class Display
{
    private ObjectFactory objectFactory;

    public List<String> getDisplayFields(Class displayHandler)
    {
        // extract the field names.
        List<String> displayFields = new LinkedList<String>();
        for (Method method : displayHandler.getMethods())
        {
            String methodName = method.getName();
            if (!methodName.startsWith("get") || methodName.length() < 4)
            {
                continue;
            }
            if (method.getParameterTypes().length > 1)
            {
                continue;
            }
            if (method.getReturnType() != String.class)
            {
                continue;
            }
            String fieldName = methodName.substring(3, 4).toLowerCase();
            if (methodName.length() > 4)
            {
                fieldName = fieldName  + methodName.substring(4);
            }
            displayFields.add(fieldName);
        }
        return displayFields;
    }

    public Object format(Class displayHandler, String fieldName, final Object obj) throws Exception
    {
        // invoke the formatter method.
        Object displayHandlerInstance = objectFactory.buildBean(displayHandler);
        final String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + (fieldName.length() > 1 ? fieldName.substring(1) : "");

        // this method may have none or one parameter.
        Method method = CollectionUtils.find(displayHandler.getMethods(), new Predicate<Method>()
        {
            public boolean satisfied(Method method)
            {
                return method.getName().equals(methodName) &&
                       (ReflectionUtils.acceptsParameters(method) || ReflectionUtils.acceptsParameters(method, obj.getClass()));
            }
        });

        if(method != null)
        {
            if(method.getParameterTypes().length == 0)
            {
                return method.invoke(displayHandlerInstance);
            }
            else
            {
                return method.invoke(displayHandlerInstance, obj);
            }
        }

        return "[method not found]";
    }

        /**
     * Required resource
     *
     * @param objectFactory instance
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
