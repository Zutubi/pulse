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

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;

/**
 * A predicate that matches a request holder containing a
 * build request event with a specified owner and options source field.
 *
 * @param <T> the specific subclass of RequestHolder that is being searched.
 */
public class HasOwnerAndSourcePredicate<T extends RequestHolder> implements Predicate<T>
{
    private Object owner;
    private String source;

    public HasOwnerAndSourcePredicate(RequestHolder request)
    {
        this(request.getOwner(), request.getRequest().getRequestSource());
    }

    public HasOwnerAndSourcePredicate(Object owner, String source)
    {
        this.owner = owner;
        this.source = source;
    }

    public boolean apply(T holder)
    {
        BuildRequestEvent request = holder.getRequest();
        return request.getOwner().equals(owner) && Objects.equal(request.getOptions().getSource(), source);
    }
}
