package com.zutubi.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

/**
 */
public class ReflectionUtils
{
    public static List<Field> getDeclaredFields(Class clazz, Class stopClazz)
    {
        if(stopClazz == null)
        {
            stopClazz = Object.class;
        }

        List<Field> result = new LinkedList<Field>();
        while(clazz != null && clazz != stopClazz)
        {
            result.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        return result;
    }
    
    /**
     * Returns true if the given method will accept parameters of the given
     * types.  Unlike {@link Class#getMethod(String, Class[])}, the parameter
     * types need not be exact matches: subtypes are also accepted.
     *
     * @param method         the method to test
     * @param parameterTypes candidate parameter types
     * @return true if the method could be called with instances of the given
     *         types
     */
    public static boolean acceptsParameters(Method method, Class... parameterTypes)
    {
        Class<?>[] actualTypes = method.getParameterTypes();
        if(actualTypes.length != parameterTypes.length)
        {
            return false;
        }

        for(int i = 0; i < actualTypes.length; i++)
        {
            if(!actualTypes[i].isAssignableFrom(parameterTypes[i]))
            {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Returns true if the given method returns a parameterised type that is
     * compatible with the given type details.  The returned type must be
     * declared generic with the given type parameters, and abe assignable to
     * the given raw class.
     *
     * @param method        the method to test
     * @param rawClass      the raw class to test the return type for
     * @param typeArguments type arguments that the return type should be
     *                      parameterised by
     * @return true if the given method returns a compatible parameterised
     *         type
     */
    public static boolean returnsParameterisedType(Method method, Class rawClass, Type... typeArguments)
    {
        if(rawClass.isAssignableFrom(method.getReturnType()))
        {
            java.lang.reflect.Type returnType = method.getGenericReturnType();
            if(returnType instanceof ParameterizedType)
            {
                ParameterizedType parameterizedType = (ParameterizedType) returnType;
                return Arrays.equals(parameterizedType.getActualTypeArguments(), typeArguments);
            }
        }

        return false;
    }
}
