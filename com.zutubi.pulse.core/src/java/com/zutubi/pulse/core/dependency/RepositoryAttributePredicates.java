/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
