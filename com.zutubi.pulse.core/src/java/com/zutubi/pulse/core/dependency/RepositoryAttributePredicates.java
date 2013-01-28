package com.zutubi.pulse.core.dependency;

import com.google.common.base.Predicate;
import com.zutubi.util.Sort;

import java.util.Comparator;
import java.util.Map;

/**
 * A set of predicates that simplify interaction with the repository attributes.
 */
public class RepositoryAttributePredicates
{
    /**
     * A predicate that matches all attributes where the name and value match the specified name and value.
     * @param attributeName     the name to be matched
     * @param attributeValue    the value to be matched
     * @return  a predicate that returns true if the attributes contains a matching name : value pair.
     */
    public static Predicate<Map<String, String>> attributeEquals(final String attributeName, final String attributeValue)
    {
        return new Predicate<Map<String, String>>()
        {
            private Comparator<String> comparator = new Sort.StringComparator();
            public boolean apply(Map<String, String> attributes)
            {
                if (attributes.containsKey(attributeName))
                {
                    return comparator.compare(attributeValue, attributes.get(attributeName)) == 0;
                }
                return false;
            }
        };
    }

}
