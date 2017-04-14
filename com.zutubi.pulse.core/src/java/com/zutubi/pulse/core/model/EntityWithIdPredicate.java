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

package com.zutubi.pulse.core.model;

import com.google.common.base.Predicate;

/**
 * Predicate to test the id of entities against a fixed id.
 */
public class EntityWithIdPredicate<T extends Entity> implements Predicate<T>
{
    private final long id;

    public EntityWithIdPredicate(long id)
    {
        this.id = id;
    }

    public boolean apply(T entity)
    {
        return entity.getId() == id;
    }
}
