package com.zutubi.tove.transaction;

import com.zutubi.i18n.Messages;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.logging.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The transaction manager is responsible for managing the systems transactions.  It begins, rolls back,
 * commits transactions, as well as tracking transaction status'.
 *
 * This transcation manager does not support partial locking of a resource. Therefore, only one transaction
 * is supported at any one time.
 */
public class TransactionManager
{
    private static final Messages I18N = Messages.getInstance(TransactionManager.class);
    private static final Logger LOG = Logger.getLogger(TransactionManager.class);

    private static final AtomicLong nextTransactionId = new AtomicLong(1);

    /**
     * The active transaction bound to the current thread. 
     */
    private ThreadLocal<Transaction> transactionHolder = new ThreadLocal<Transaction>();

    /**
     * Lock used to serialise transactions.
     */
    private Lock activeTransaction = new ReentrantLock();

    public TransactionManager()
    {
    }

    /**
     * Retrieve the transaction associated with the current thread.
     *
     * @return a transaction instance if one is available.
     */
    public Transaction getTransaction()
    {
        return transactionHolder.get();
    }

    /**
     * Begin a transaction on the current thread.  This creates a new transaction if one does
     * not already exist, or joins an existing transaction if it does.
     *
     * @return the active transaction.
     */
    public Transaction begin()
    {
        // Indicate that a transaction is in progress. If that transaction is not associated with the
        // current thread, then we wait until it is completed before continuing.  We are effectively
        // waiting on the transaction lock becoming available.

        Transaction txn = transactionHolder.get();
        if (txn == null)
        {
            activeTransaction.lock();

            // No transaction in progress, so start one.
            txn = new Transaction(nextTransactionId.getAndIncrement(), this);
            txn.setStatus(TransactionStatus.ACTIVE);
            transactionHolder.set(txn);
        }
        else
        {
            // track the transaction depth...
            txn.setDepth(txn.getDepth() + 1);
        }
        return txn;
    }

    /**
     * Commit the currently active transaction.  This requires that a transaction
     * be active or an error is thrown.
     *
     * If the current transaction is marked as rollbackOnly or a problem occurs
     * during the commit, a rollback will be triggered.
     *
     * @throws TransactionException if no transaction is active or if there is a
     * problem committing the transaction.
     */
    public void commit() throws TransactionException
    {
        Transaction currentTransaction = transactionHolder.get();
        if (currentTransaction == null)
        {
            throw new TransactionException(I18N.format("no.active.transaction"));
        }

        // check the txn depth.
        if (currentTransaction.getDepth() > 0)
        {
            currentTransaction.setDepth(currentTransaction.getDepth() - 1);
            return;
        }

        if (isRollbackOnly(currentTransaction))
        {
            // fork into the standard rollback processing.
            rollback();
            return;
        }

        TransactionStatus currentStatus = currentTransaction.getStatus();
        if (currentStatus != TransactionStatus.ACTIVE)
        {
            throw new TransactionException(I18N.format("commit.non-active", currentStatus));
        }

        currentTransaction.setStatus(TransactionStatus.COMMITTING);

        boolean canCommit = true;
        for (TransactionResource resource : currentTransaction.getResources())
        {
            try
            {
                canCommit = resource.prepare();
            }
            catch (Throwable e)
            {
                LOG.warning(I18N.format("prepare.failed"), e);
                canCommit = false;
            }

            if (!canCommit)
            {
                break;
            }
        }

        if (canCommit)
        {
            List<TransactionResource> resources = new LinkedList<TransactionResource>(currentTransaction.getResources());
            for (TransactionResource resource : resources)
            {
                resource.commit();
            }

            currentTransaction.setStatus(TransactionStatus.COMMITTED);

            transactionHolder.set(null);
            activeTransaction.unlock();

            List<Synchronisation> synchronisations = new LinkedList<Synchronisation>(currentTransaction.getSynchronisations());
            for (Synchronisation synchronisation : synchronisations)
            {
                synchronisation.postCompletion(currentTransaction);
            }
        }
        else
        {
            rollback();

            throw new RollbackException(I18N.format("prepare.failed"));
        }
    }

    /**
     * Rollback any changed made in the current threads transaction.  All resources registered with
     * that transaction will be notified that they should rollback any local changes.
     *
     * @throws TransactionException if no active transaction is associated with the current thread.
     */
    public void rollback() throws TransactionException
    {
        Transaction currentTransaction = transactionHolder.get();
        if (currentTransaction == null)
        {
            throw new TransactionException(I18N.format("no.active.transaction"));
        }

        // check the txn depth.
        if (currentTransaction.getDepth() > 0)
        {
            currentTransaction.setDepth(currentTransaction.getDepth() - 1);
            currentTransaction.setStatus(TransactionStatus.ROLLBACKONLY);
            return;
        }

        currentTransaction.setStatus(TransactionStatus.ROLLINGBACK);

        List<TransactionResource> resources = new LinkedList<TransactionResource>(currentTransaction.getResources());
        for (TransactionResource resource : resources)
        {
            resource.rollback();
        }

        currentTransaction.setStatus(TransactionStatus.ROLLEDBACK);

        transactionHolder.set(null);
        activeTransaction.unlock();

        List<Synchronisation> synchronisations = new LinkedList<Synchronisation>(currentTransaction.getSynchronisations());
        for (Synchronisation synchronisation : synchronisations)
        {
            synchronisation.postCompletion(currentTransaction);
        }
    }

    /**
     * Mark the transaction associated with the current thread as 'rollback only'.  This indicates
     * that this transaction cannot be committed.
     *
     * @throws TransactionException if no transaction is associated with the current thread.
     */
    public void setRollbackOnly() throws TransactionException
    {
        Transaction activeTransaction = transactionHolder.get();
        if (activeTransaction == null)
        {
            throw new TransactionException(I18N.format("no.active.transaction"));
        }
        activeTransaction.setStatus(TransactionStatus.ROLLBACKONLY);
    }

    /**
     * Returns true if the transaction associated with the current thread is marked as rollback only.
     *
     * @return true if the current threads transaction can only be rolled back
     */
    public boolean isRollbackOnly()
    {
        return isRollbackOnly(transactionHolder.get());
    }

    private boolean isRollbackOnly(Transaction transaction)
    {
        return transaction != null && transaction.getStatus() == TransactionStatus.ROLLBACKONLY;
    }

    /**
     * Returns true if a transaction is active on the current thread.
     *
     * @return true if a transaction is active, false otherwise.
     */
    public boolean isTransactionActive()
    {
        return transactionHolder.get() != null;
    }

    /**
     * Get the status of the transaction associated with the current thread.
     *
     * @return the transaction status
     *
     * @throws TransactionException if no transaction is associated with the current thread.
     */
    public TransactionStatus getStatus() throws TransactionException
    {
        Transaction activeTransaction = transactionHolder.get();
        if (activeTransaction == null)
        {
            throw new TransactionException(I18N.format("no.active.transaction"));
        }
        return activeTransaction.getStatus();
    }

    /**
     * Run the provided function within the context of a transaction.  If a transaction is
     * active, then this function participates in that existing transaction.  If not, a new
     * transaction is started.
     *
     * @param function      the function to be run within the context of a transaction.
     * @param resources     resources to be bound to the transaction.
     * @param <T>           the return type of the function.
     * @return the result of the function.
     */
    public <T> T runInTransaction(final NullaryFunction<T> function, TransactionResource... resources)
    {
        final ResultHolder<T> holder = new ResultHolder<T>();
        inTransaction(new Runnable()
        {
            public void run()
            {
                holder.result = function.process();
            }
        }, resources);

        return holder.result;
    }

    private class ResultHolder<T>
    {
        T result;
    }

    /**
     * Run the provided procedure within the context of a transaction.  If a transaction is
     * active, then this procedure participates in that existing transaction.  If not, a new
     * transaction is started.
     *
     * @param procedure     the procedure to be run within the context of a transaction.
     * @param resources     resources to be bound to the transaction.
     */
    public void runInTransaction(Runnable procedure, TransactionResource... resources)
    {
        inTransaction(procedure, resources);
    }

    private void inTransaction(Runnable procedure, TransactionResource... resources)
    {
        // ensure that we are part of the transaction.
        boolean activeTransaction = getTransaction() != null;
        if (!activeTransaction)
        {
            // execute a manual transaction.
            begin();
        }

        for (TransactionResource resource: resources)
        {
            getTransaction().enlistResource(resource);
        }

        if (!activeTransaction)
        {
            try
            {
                procedure.run();

                // execute a manual transaction.
                commit();
            }
            catch (RuntimeException e)
            {
                rollback();
                throw e;
            }
            catch (Throwable t)
            {
                rollback();
                throw new RuntimeException(t);
            }
        }
        else
        {
            try
            {
                procedure.run();
            }
            catch (RuntimeException e)
            {
                setRollbackOnly();
                throw e;
            }
            catch (Throwable t)
            {
                setRollbackOnly();
                throw new RuntimeException(t);
            }
        }
    }
}