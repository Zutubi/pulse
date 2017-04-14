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

package com.zutubi.util.reflection;

import com.google.common.base.Predicate;

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

    public boolean apply(Method method)
    {
        String name = method.getName();
        return name.startsWith(prefix) && (allowExactMatch || name.length() > prefix.length());
    }
}
