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

import java.util.LinkedList;
import java.util.List;

/**
 * An InMemoryStateWrapper implementation that holds a list.
 *
 * Note that it is only the lists structure that is managed by this
 * in memory state wrapper, not the contents of the list.
 *
 * @param <U>   the type of value held by the list.
 */
public class InMemoryListStateWrapper<U> extends InMemoryStateWrapper<List<U>>
{
    public InMemoryListStateWrapper(List<U> state)
    {
        super(state);
    }

    protected InMemoryStateWrapper<List<U>> copy()
    {
        return new InMemoryListStateWrapper<U>(new LinkedList<U>(get()));
    }
}