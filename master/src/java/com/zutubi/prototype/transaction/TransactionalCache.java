package com.zutubi.prototype.transaction;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public abstract class TransactionalCache<T> implements TransactionResource
{
    private Map<String, T> globalCache = new HashMap<String,T>();

    private ThreadLocal<Map<String, T>> threadsCache = new ThreadLocal<Map<String,T>>();

    private TransactionManager transactionManager;

    public T get(String key)
    {
        return cache().get(key);
    }

    public void set(String key, T value)
    {
        cache().put(key, value);
    }

    public Object execute(String key, Action<T> action)
    {
        // ensure that we are part of the transaction.
        boolean activeTransaction = transactionManager.getTransaction() != null;
        if (activeTransaction)
        {
            transactionManager.getTransaction().enlistResource(this);
        }

        Map<String, T> currentThreadCache = threadsCache.get();
        if (currentThreadCache == null)
        {
            currentThreadCache = new HashMap<String,T>();
            threadsCache.set(currentThreadCache);
        }

        T writeableState = currentThreadCache.get(key);
        if (writeableState == null)
        {
            T globalState = globalCache.get(key);
            writeableState = copy(globalState);
            currentThreadCache.put(key, writeableState);
        }

        Object result = action.execute(writeableState);
        if (!activeTransaction)
        {
            if (prepare())
            {
                commit();
            }
            else
            {
                rollback();
            }
        }
        return result;
    }

    public boolean prepare()
    {
        return true;
    }

    public void commit()
    {
        // copy the current threads cache to the global cache
        Map<String, T> threadCache = threadsCache.get();
        if (threadCache != null)
        {
            globalCache = threadCache;
            threadsCache.set(null);
        }
    }

    public void rollback()
    {
        // clean out the current threads cache.
        threadsCache.set(null);
    }

    private Map<String, T> cache()
    {
        Map<String, T> cache = threadsCache.get();
        if (cache == null)
        {
            cache = this.globalCache;
        }
        return cache;
    }

    public void setTransactionManager(TransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    public static interface Action<T>
    {
        Object execute(T t);
    }

    public abstract T copy(T v);
}
