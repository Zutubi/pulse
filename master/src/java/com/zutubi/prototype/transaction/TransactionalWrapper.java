package com.zutubi.prototype.transaction;

/**
 *
 */
public abstract class TransactionalWrapper<T> implements TransactionResource
{
    private T global;

    private ThreadLocal<T> threadlocal = new ThreadLocal<T>();

    private TransactionManager transactionManager;

    protected TransactionalWrapper(T global)
    {
        this.global = global;
    }

    public T get()
    {
        if (threadlocal.get() != null)
        {
            return threadlocal.get();
        }
        return global;
    }

    public Object execute(Action<T> action)
    {
        // ensure that we are part of the transaction.
        boolean activeTransaction = transactionManager.getTransaction() != null;
        if (activeTransaction)
        {
            // join existing transaction.
            transactionManager.getTransaction().enlistResource(this);
        }
        else
        {
            // execute a manual transaction.
            transactionManager.begin();
            transactionManager.getTransaction().enlistResource(this);
        }

        T writeableState = threadlocal.get();
        if (writeableState == null)
        {
            writeableState = copy(global);
            threadlocal.set(writeableState);
        }

        Object result = action.execute(writeableState);
        if (!activeTransaction)
        {
            // execute a manual transaction.
            transactionManager.commit();
        }
        return result;
    }

    //---( transactional resource implementation )---

    public boolean prepare()
    {
        return true;
    }

    public void commit()
    {
        // copy the changes made on the current threads cache to
        // the global cache.
        T t = threadlocal.get();
        if (t != null)
        {
            global = t;
            threadlocal.set(null);
        }
    }

    public void rollback()
    {
        // rollback all of the changes made on the current threads transaction.
        threadlocal.set(null);
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
