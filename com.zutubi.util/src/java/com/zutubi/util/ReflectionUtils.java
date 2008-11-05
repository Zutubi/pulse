package com.zutubi.util;

import java.lang.reflect.*;
import java.util.*;

/**
 * A collection of useful methods for reflection.  Essentially a higher-level
 * interface to java.lang.reflect, which is quite low-level.
 */
public class ReflectionUtils
{
    /**
     * Returns all superclasses of the given class, up to but not including the
     * stop class.  If the stop class is not an ancestor, or is null, all super
     * classes including Object.class are returned.  Note that super interfaces
     * are not included.
     *
     * @param clazz     class to get super classes of
     * @param stopClazz ancestor class at which to stop, will not be included
     *                  in the result, may be null to indicate all ancestors
     *                  should be returned
     * @param strict    if true, the class itself is not included, if false it
     *                  is
     * @return a list of all superclasses of the given class before the stop
     *         class ordered from closest to furthest ancestor
     */
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

    /**
     * Returns the set of all supertypes (superclasses and implemented
     * interfaces) of the given class by traversing the inheritance hierarchy
     * up to but not including the stop class.  Implemented interfaces will
     * include those implemented directly or indirectly (by interface
     * inheritance) by all traversed classes.  If the stop class is null or is
     * not an ancestor of the given class, all supertypes will be included up
     * to and including Object.class.
     *
     * @param clazz     the class to retrieve supertypes for
     * @param stopClazz ancestor class at which to stop traversing up the
     *                  hierarchy to find super types.  This class will not be
     *                  included in the result.  May be null to indicate all
     *                  supertypes should be returned.
     * @param strict    if true, the class itself is not included, if false it
     *                  is included
     * @return the set of all supertypes for the given class found by
     *         traversing the hierarchy up to the stop class
     */
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

    /**
     * Returns the set of all interfaces implemented directly or indirectly by
     * the given class within its ancestry up to but not including the given
     * stop class.  This is equivalent to calling {@link #getSupertypes(Class, Class, boolean)}
     * with the same arguments and filtering the result to only include
     * interfaces.
     *
     * @param clazz     the class to retrieve implmented interfaces for
     * @param stopClazz the class at which to stop traversing the ancestry
     *                  looking for interfaces, may be null to indicate the
     *                  entire ancestry should be traversed
     * @param strict    if true the class itself is not include, if false it
     *                  will be included if it represents an interface
     * @return the set of all interfaces implemented directly or indirectly by
     *         the given class within its ancestry up to but not including the
     *         stop class
     *
     * @see #getSupertypes(Class, Class, boolean)
     */
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

    /**
     * Returns all fields declared by a class and all of its supertypes up to
     * but not including the stop class.  This is all fields declared on all
     * types found by calling {@code getSupertypes(clazz, stopClazz, false)}.
     *
     * @param clazz     class to get the declared fields for
     * @param stopClazz ancestor class at which to stop traversing the
     *                  hierarchy, may be null to indicate the entire hierarchy
     *                  should be traversed
     * @return the set of all fields declared by the given class and its
     *         supertypes found by traversing the hierachy up to the stop class
     *
     * @see #getSupertypes(Class, Class, boolean)
     */
    public static Set<Field> getDeclaredFields(Class clazz, Class stopClazz)
    {
        Set<Field> result = new HashSet<Field>();
        for (Class c: getSupertypes(clazz, stopClazz, false))
        {
            result.addAll(Arrays.asList(c.getDeclaredFields()));
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
     * declared generic with the given type parameters, and be assignable to
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

    /**
     * Returns the name of the member qualified with its declaring class.
     *
     * @param member member to get the name for
     * @return &lt;declaring class&gt;.&lt;member name&gt;
     */
    public static String getQualifiedName(Member member)
    {
        return member.getDeclaringClass().getName() + "." + member.getName();
    }

    /**
     * Returns true if a member is declared as final.
     *
     * @param member the member to test
     * @return true if the member was declared final
     */
    public static boolean isFinal(Member member)
    {
        return Modifier.isFinal(member.getModifiers());
    }

    /**
     * Sets the given instance's given field to the given value via reflection,
     * working around any accessibility constraint (e.g. fields declared
     * private) if possible.  Note that final fields can not be set in this
     * way.
     *
     * @param instance the instance to set the field on
     * @param field    the field to set, must not be final
     * @param value    the new field value, automatically unwrapped if the
     *                 field is primitive
     * @throws IllegalArgumentException if the field does not match the
     *         instance, is final or may not be made accessible
     *
     * @see Field#set(Object, Object) 
     */
    public static void setFieldValue(Object instance, Field field, Object value)
    {
        if (isFinal(field))
        {
            throw new IllegalArgumentException("Cannot set final field '" + getQualifiedName(field) + "', even if it succeeds it may have no effect due to compiler optimisations");
        }

        if (!field.isAccessible())
        {
            field.setAccessible(true);
        }

        try
        {
            field.set(instance, value);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalArgumentException("Field '" + getQualifiedName(field) + "' cannot be set despite ensuring accessibility");
        }
    }
}
