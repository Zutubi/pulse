package com.zutubi.util.reflection;

import com.zutubi.util.ConjunctivePredicate;
import com.zutubi.util.DisjunctivePredicate;
import com.zutubi.util.Predicate;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Static helpers for building method predicates.
 */
public class MethodPredicates
{
    public static Predicate<Method> and(Predicate<Method>... ps)
    {
        return new ConjunctivePredicate<Method>(ps);
    }

    public static Predicate<Method> or(Predicate<Method>... ps)
    {
        return new DisjunctivePredicate<Method>(ps);
    }

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
