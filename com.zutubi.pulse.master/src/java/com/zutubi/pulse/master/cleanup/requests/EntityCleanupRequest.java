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

package com.zutubi.pulse.master.cleanup.requests;

import com.zutubi.pulse.core.model.Entity;

/**
 * A base cleanup request for entity instances that implements the equals and
 * hashCode methods such that requests for the same entity are considered the
 * same. 
 */
public abstract class EntityCleanupRequest implements Runnable
{
    private Entity entity;

    protected EntityCleanupRequest(Entity entity)
    {
        this.entity = entity;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        EntityCleanupRequest that = (EntityCleanupRequest) o;
        return entity.equals(that.entity);
    }

    public int hashCode()
    {
        return entity.hashCode();
    }
}
