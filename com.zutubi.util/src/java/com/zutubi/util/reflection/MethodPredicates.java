package com.zutubi.util.reflection;


import java.lang.reflect.Type;

/**
 * Static helpers for building method predicates.
 */
public class MethodPredicates
{
    public static MethodNameEqualsPredicate hasName(String name)
    {
        return new MethodNameEqualsPredicate(name);
    }

    public static MethodNamePrefixPredicate hasPrefix(String prefix, boolean acceptExactMatch)
    {
        return new MethodNamePrefixPredicate(prefix, acceptExactMatch);
    }

    public static MethodReturnsTypePredicate returnsType(Class<?> rawType, Type... typeArguments)
    {
        return new MethodReturnsTypePredicate(rawType, typeArguments);
    }

    public static MethodAcceptsParametersPredicate acceptsParameters(Class<?>... types)
    {
        return new MethodAcceptsParametersPredicate(types);
    }
}
