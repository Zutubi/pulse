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

package com.zutubi.pulse.master.build.queue;

import com.google.common.base.Predicate;

/**
 * A predicate that matches a request holder containing a
 * build request event with a specified id.
 *
 * @param <T> the specific subclass of RequestHolder that is being searched.
 */
public class HasIdPredicate<T extends RequestHolder> implements Predicate<T>
{
    private long id;

    public HasIdPredicate(long id)
    {
        this.id = id;
    }

    public boolean apply(T holder)
    {
        return holder.getRequest().getId() == id;
    }
}
