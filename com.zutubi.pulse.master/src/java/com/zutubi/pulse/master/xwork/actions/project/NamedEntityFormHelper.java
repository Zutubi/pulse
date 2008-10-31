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
