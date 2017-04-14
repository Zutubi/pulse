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

import java.util.HashSet;
import java.util.Set;

/**
 * A transitive version of a mapping from an instance to a set.  Given a
 * function representing the mapping, this function will act as the transitive
 * version of that mapping.  That is, the set produced by this function
 * includes all instances reachable by recursive application of the original
 * function.
 */
public class TransitiveFunction<T> implements Function<T, Set<T>>
{
    private Function<T, Set<T>> directFn;

    public TransitiveFunction(Function<T, Set<T>> directFn)
    {
        this.directFn = directFn;
    }

    public Set<T> apply(T t)
    {
        Set<T> result = new HashSet<T>();
        addAll(directFn, t, result);
        return result;
    }

    private void addAll(Function<T, Set<T>> directFn, T t, Set<T> result)
    {
        for (T u: directFn.apply(t))
        {
            if (result.add(u))
            {
                addAll(directFn, u, result);
            }
        }
    }
}
