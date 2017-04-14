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

/**
 * 
 *
 */
public class Entity
{
    private long id;

    private static final int UNSAVED = 0;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public boolean isPersistent()
    {
        return this.id != UNSAVED;
    }

    public boolean equals(Object other)
    {
        if (!(other instanceof Entity))
        {
            return false;
        }
        Entity otherEntity = (Entity) other;
        if (id == UNSAVED || otherEntity.id == UNSAVED)
        {
            return false;
        }
        return id == otherEntity.id;
    }

    public int hashCode()
    {
        return Long.valueOf(id).hashCode();
    }
}
