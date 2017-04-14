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

package com.zutubi.pulse.core.plugins.util;

import com.google.common.base.Function;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Fake dependent-generating function for testing.
 */
class FixedDependentsFunction implements Function<String, Set<String>>
{
    private Map<String, Set<String>> dependencyMapping = new HashMap<String, Set<String>>();

    /**
     * Adds a string with a fixed set of dependents.
     *
     * @param s    the string to add
     * @param deps set of dependents for the string
     */
    public void add(String s, String... deps)
    {
        Set<String> set = get(s);
        set.addAll(java.util.Arrays.asList(deps));
    }

    public Set<String> apply(String s)
    {
        return get(s);
    }

    private Set<String> get(String s)
    {
        Set<String> set = dependencyMapping.get(s);
        if (set == null)
        {
            set = new HashSet<String>();
            dependencyMapping.put(s, set);
        }
        return set;
    }
}
