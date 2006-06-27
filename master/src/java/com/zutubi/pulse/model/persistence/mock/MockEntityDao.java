package com.zutubi.pulse.model.persistence.mock;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.model.persistence.EntityDao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public abstract class MockEntityDao<T extends Entity> implements EntityDao<T>
{
    private long id = 1;

    protected List<T> entities = new LinkedList<T>();

    public void delete(T entity)
    {
        entities.remove(entity);
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

    public <U extends T> U findByIdAndType(long id, Class<U> type)
    {
        for (T t : entities)
        {
            if (t.getId() == id)
            {
                return (U) t;
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
        }
        else
        {
            // save.
            setId(entity, nextId());
            entities.add(entity);
        }
    }

    protected List<T> findByFilter(Filter<T> f)
    {
        LinkedList<T> findResults = new LinkedList<T>();
        for (T t : entities)
        {
            if (f.include(t))
            {
                findResults.add(t);
            }
        }
        return findResults;
    }

    protected T findUniqueByFilter(Filter<T> f)
    {
        List<T> findResults = findByFilter(f);
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

    protected interface Filter<T>
    {
        boolean include(T o);
    }
}
