package com.zutubi.tove.transaction;

import com.zutubi.util.UnaryFunction;

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

    public <U> U execute(UnaryFunction<T, U> action)
    {
        // ensure that we are part of the transaction.
        boolean activeTransaction = transactionManager.getTransaction() != null;
        if (!activeTransaction)
        {
            // execute a manual transaction.
            transactionManager.begin();
        }

        transactionManager.getTransaction().enlistResource(this);

        T writeableState = threadlocal.get();
        if (writeableState == null)
        {
            writeableState = copy(global);
            threadlocal.set(writeableState);
        }

        if (!activeTransaction)
        {
            try
            {
                U result = action.process(writeableState);

                // execute a manual transaction.
                transactionManager.commit();

                return result;
            }
            catch (RuntimeException e)
            {
                transactionManager.rollback();
                throw e;
            }
            catch (Throwable t)
            {
                transactionManager.rollback();
                throw new RuntimeException(t);
            }
        }
        else
        {
            try
            {
                return action.process(writeableState);
            }
            catch (RuntimeException e)
            {
                transactionManager.setRollbackOnly();
                throw e;
            }
            catch (Throwable t)
            {
                transactionManager.setRollbackOnly();
                throw new RuntimeException(t);
            }
        }
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

    public abstract T copy(T v);
}
