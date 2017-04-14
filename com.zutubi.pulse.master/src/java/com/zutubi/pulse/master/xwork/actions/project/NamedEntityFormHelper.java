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

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.core.model.NamedEntityComparator;

import java.util.*;

/**
 * Utilities for forms that deal with projects.
 */
public abstract class NamedEntityFormHelper<T extends NamedEntity>
{
    protected abstract T get(long id);
    protected abstract List<T> getAll();

    public Map<Long, String> getAllEntities()
    {
        List<T> all = getAll();
        Collections.sort(all, new NamedEntityComparator());

        Map<Long, String> result = new LinkedHashMap<Long, String>();
        for(T t: all)
        {
            result.put(t.getId(), t.getName());
        }

        return result;
    }

    public void convertFromIds(Collection<Long> entities, Collection<T> destination)
    {
        destination.clear();
        if (entities != null)
        {
            for(Long id: entities)
            {
                T t = get(id);
                if(t != null)
                {
                    destination.add(t);
                }
            }
        }
    }

    public List<Long> convertToIds(Collection<T> entities)
    {
        List<Long> result = new ArrayList<Long>(entities.size());
        for(T t: entities)
        {
            result.add(t.getId());
        }

        return result;
    }
}
