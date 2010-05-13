package com.zutubi.tove.transaction.inmemory;

import com.zutubi.tove.transaction.TransactionResource;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.transaction.Transaction;
import com.zutubi.i18n.Messages;

/**
 * An implemenation of the {@link TransactionResource} interface that manages
 * an object in memory.
 *
 * When ever an in memory transaction resource is accessed within the scope
 * of a transaction, the resource enlists itself with the transaction and
 * manage its state accordingly.
 *
 * Read requests will ensure that a consistent view is available for the duration
 * of the transaction, and write requests will ensure that any changes to the state
 * are isolated from other threads / transactions until the transaction is committed. 
 *
 * @param <T>   the type of the object being managed.
 */
public class InMemoryTransactionResource<T> implements TransactionResource
{
    private static final Messages I18N = Messages.getInstance(InMemoryTransactionResource.class);

    /**
     * The global state is what is considered to be the currently
     * 'committed' state.  This is what is visible to new transactions.
     */
    private InMemoryStateWrapper<T> globalState;

    /**
     * The local state is the version of the state assocaited with the current
     * transaction.  This is to ensure that for the duration of a transaction,
     * a consistent view of the data is available.
     */
    private ThreadLocal<InMemoryStateWrapper<T>> localState = new ThreadLocal<InMemoryStateWrapper<T>>();

    /**
     * The system transaction manager.
     */
    private TransactionManager transactionManager;

    public InMemoryTransactionResource(InMemoryStateWrapper<T> global)
    {
        this.globalState = global;
    }

    /**
     * This method provides access to the state being managed by this transaction
     * resource.
     *
     * You can only request a writable state within the context of an active
     * transaction.
     *
     * @param writableState    true if the state may be modified, false otherwise.
     *  
     * @return the state instance.
     */
    public synchronized T get(boolean writableState)
    {
        Transaction transaction = transactionManager.getTransaction();
        if (transaction == null)
        {
            if (writableState)
            {
                throw new IllegalStateException(I18N.format("writable.requires.transaction"));
            }
            return globalState.get();
        }

        // ensure that we are enlisted with the transaction.
        transaction.enlistResource(this);

        if (writableState)
        {
            if (localState.get() == null)
            {
                InMemoryStateWrapper<T> value = globalState.copy();
                value.setDirty(true);
                localState.set(value);
            }
            else if (!localState.get().isDirty())
            {
                InMemoryStateWrapper<T> value = localState.get().copy();
                value.setDirty(true);
                localState.set(value);
            }
        }
        else
        {
            if (localState.get() == null)
            {
                localState.set(globalState);
            }
        }
        return localState.get().get();
    }

    //---( transactional resource implementation )---

    public boolean prepare()
    {
        return true;
    }

    public synchronized void commit()
    {
        // Make any local changes globally available.
        if (localState.get().isDirty())
        {
            globalState = localState.get();
            globalState.setDirty(false);
        }

        localState.set(null);
    }

    public void rollback()
    {
        localState.set(null);
    }

    public void setTransactionManager(TransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }
}