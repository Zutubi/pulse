package com.zutubi.prototype.format;

import com.zutubi.util.bean.ObjectFactory;

import java.util.List;
import java.util.LinkedList;
import java.lang.reflect.Method;

/**
 *
 *
 */
//FIXME: Need a better name for this.
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

    public Object format(Class displayHandler, String fieldName, Object obj) throws Exception
    {
        // invoke the formatter method.
        Object displayHandlerInstance = objectFactory.buildBean(displayHandler);
        String methodName = "get" + fieldName.substring(0, 1).toUpperCase();
        if (fieldName.length() > 1)
        {
            methodName = methodName + fieldName.substring(1);
        }

        // this method may have none or one parameter.
        try
        {
            Method method = displayHandler.getMethod(methodName, obj.getClass());
            return method.invoke(displayHandlerInstance, obj);
        }
        catch (NoSuchMethodException e)
        {
            // noop.
        }
        
        Method method = displayHandler.getMethod(methodName);
        return method.invoke(displayHandlerInstance);
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
