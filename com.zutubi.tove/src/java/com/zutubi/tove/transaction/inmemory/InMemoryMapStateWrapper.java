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

package com.zutubi.tove.transaction.inmemory;

import java.util.Map;
import java.util.HashMap;

/**
 * An InMemoryStateWrapper implementation that holds a map.
 *
 * Note that it is only the map structure that is managed by this
 * in memory state wrapper, not the contents of the map.
 *
 * @param <U>   the maps key type.
 * @param <V>   the maps value type.
 */
public class InMemoryMapStateWrapper<U, V> extends InMemoryStateWrapper<Map<U, V>>
{
    public InMemoryMapStateWrapper(Map<U, V> state)
    {
        super(state);
    }

    protected InMemoryStateWrapper<Map<U, V>> copy()
    {
        return new InMemoryMapStateWrapper<U, V>(new HashMap<U, V>(get()));
    }
}
