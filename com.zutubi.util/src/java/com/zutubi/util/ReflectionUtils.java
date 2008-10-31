package com.zutubi.util;

import java.lang.reflect.*;
import java.util.*;

/**
 */
public class ReflectionUtils
{
    public static List<Class> getSuperclasses(Class clazz, Class stopClazz, boolean strict)
    {
        List<Class> superClasses = new LinkedList<Class>();

        if(strict)
        {
            if(clazz == stopClazz)
            {
                return superClasses;
            }

            clazz = clazz.getSuperclass();
        }

        while(clazz != null && clazz != stopClazz)
        {
            superClasses.add(clazz);
            clazz = clazz.getSuperclass();
        }

        return superClasses;
    }

    private static Set<Class> getInterfacesTransitive(Class clazz)
    {
        Queue<Class> toProcess = new LinkedList<Class>();
        toProcess.addAll(Arrays.asList(clazz.getInterfaces()));
        Set<Class> superInterfaces = new HashSet<Class>();
        while(!toProcess.isEmpty())
        {
            clazz = toProcess.remove();
            superInterfaces.add(clazz);
            toProcess.addAll(Arrays.asList(clazz.getInterfaces()));
        }

        return superInterfaces;
    }

    public static Set<Class> getSupertypes(Class clazz, Class stopClazz, boolean strict)
    {
        List<Class> superClasses = getSuperclasses(clazz, stopClazz, false);
        Set<Class> superTypes = new HashSet<Class>();
        for(Class superClazz: superClasses)
        {
            if(superClazz != clazz || !strict)
            {
                superTypes.add(superClazz);
            }

            superTypes.addAll(getInterfacesTransitive(superClazz));
        }

        return superTypes;
    }

    public static Set<Class> getImplementedInterfaces(Class clazz, Class stopClazz, boolean strict)
    {
        return CollectionUtils.filter(getSupertypes(clazz, stopClazz, strict), new Predicate<Class>()
        {
            public boolean satisfied(Class aClass)
            {
                return Modifier.isInterface(aClass.getModifiers());
            }
        }, new HashSet<Class>());
    }

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

    /**
     * Traverse the classes class hierarchy.  For each class class, the interfaces directly implemented
     * by that class are also traversed.  Note that this does not include interfaces implemented by interfaces.
     *
     * @param clazz     the class being traversed.
     * @param c         the procedure to be executed for each new Class encountered during the traversal.
     */
    public static void traverse(Class clazz, UnaryProcedure<Class> c)
    {
        Set<Class> checked = new HashSet<Class>();

        while (clazz != null)
        {
            c.process(clazz);
            checked.add(clazz);

            // for each class, analyse the interfaces.
            for (Class interfaceClazz : clazz.getInterfaces())
            {
                if (!checked.contains(interfaceClazz))
                {
                    c.process(interfaceClazz);
                    checked.add(interfaceClazz);
                }
            }

            clazz = clazz.getSuperclass();
        }
    }
}
