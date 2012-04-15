package com.zutubi.pulse.master.model.persistence;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.util.Predicate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A trivial implementation of {@link EntityDao} which can be used for testing.
 */
public class InMemoryEntityDao<T extends Entity> implements EntityDao<T>
{
    private long id = 1;

    protected List<T> entities = new LinkedList<T>();

    public void delete(T entity)
    {
        entities.remove(entity);
    }

    public void flush()
    {

    }

    public int deleteAll(Collection<T> toDelete)
    {
        int deleted = 0;
        for (T t: toDelete)
        {
            if (entities.remove(t))
            {
                deleted++;
            }
        }

        return deleted;
    }

    public int deleteByPredicate(Predicate<T> p)
    {
        return deleteAll(findByPredicate(p));
    }

    public int count()
    {
        return entities.size();
    }

    public List<T> findAll()
    {
        return new LinkedList<T>(entities);
    }

    public T findById(long id)
    {
        for (T t : entities)
        {
            if (t.getId() == id)
            {
                return t;
            }
        }
        return null;
    }

    public void refresh(T entity)
    {

    }

    public void save(T entity)
    {
        if (entities.contains(entity))
        {
            // update.
            entities.remove(entity);
            entities.add(entity);
        }
        else
        {
            // save.
            setId(entity, nextId());
            entities.add(entity);
        }
    }

    protected List<T> findByPredicate(Predicate<T> f)
    {
        LinkedList<T> findResults = new LinkedList<T>();
        for (T t : entities)
        {
            if (f.satisfied(t))
            {
                findResults.add(t);
            }
        }
        return findResults;
    }

    protected T findUniqueByPredicate(Predicate<T> f)
    {
        List<T> findResults = findByPredicate(f);
        if (findResults.size() > 1)
        {
            throw new RuntimeException();
        }
        if (findResults.size() == 1)
        {
            return findResults.get(0);
        }
        return null;
    }

    private long nextId()
    {
        return id++;
    }

    private void setId(Entity entity, long id)
    {
        Class cls = entity.getClass();
        try
        {
            while (cls != null)
            {
                try
                {

                    Method m = cls.getDeclaredMethod("setId", long.class);
                    m.setAccessible(true);
                    m.invoke(entity, id);
                    return;
                }
                catch (NoSuchMethodException e)
                {
                    cls = cls.getSuperclass();
                }
            }
            throw new RuntimeException("setId not found.");
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }
}
