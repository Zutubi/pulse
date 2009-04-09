package com.zutubi.util.reflection;

import com.zutubi.util.Predicate;

import java.lang.reflect.Method;

/**
 * A predicate to test if a method name starts with a given prefix.  Can
 * optionally check that the name includes more characters than just the
 * prefix.
 */
public class MethodNamePrefixPredicate implements Predicate<Method>
{
    private String prefix;
    private boolean allowExactMatch;

    /**
     * Create a predicate matching methods starting with prefix.
     * 
     * @param prefix          prefix to test for
     * @param allowExactMatch if true, a method with exactly the prefix as its
     *                        name is accepted, otherwise the name must include
     *                        at least one more character
     */
    public MethodNamePrefixPredicate(String prefix, boolean allowExactMatch)
    {
        this.prefix = prefix;
        this.allowExactMatch = allowExactMatch;
    }

    public boolean satisfied(Method method)
    {
        String name = method.getName();
        return name.startsWith(prefix) && (allowExactMatch || name.length() > prefix.length());
    }
}
