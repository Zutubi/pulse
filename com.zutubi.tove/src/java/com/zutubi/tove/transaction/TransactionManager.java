package com.zutubi.tove.transaction;

import com.zutubi.util.logging.Logger;

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
    private static final Logger LOG = Logger.getLogger(TransactionManager.class);

    private static long nextTransactionId = 1;

    private ThreadLocal<Transaction> transactionHolder = new ThreadLocal<Transaction>();

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
            // No transaction in progress, so start one.
            txn = new Transaction(nextTransactionId++, this);
            txn.setStatus(TransactionStatus.ACTIVE);
            transactionHolder.set(txn);

            activeTransaction.lock();

        }
        else
        {
            // track the transaction depth...
            txn.setDepth(txn.getDepth() + 1);
        }

        return txn;
    }

    public void commit() throws TransactionException
    {
        Transaction currentTransaction = transactionHolder.get();
        if (currentTransaction == null)
        {
            throw new TransactionException("No active transaction available.");
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
            throw new TransactionException("Attempting to commit a non-active transaction. Current transaction status is " + currentStatus.toString().toLowerCase());
        }

        currentTransaction.setStatus(TransactionStatus.COMMITTING);

        // look through the enlisted resources, and
        boolean canCommit = true;
        for (TransactionResource resource : currentTransaction.getResources())
        {
            try
            {
                canCommit = resource.prepare();
            }
            catch (Throwable e)
            {
                LOG.warning("Failed to prepare transaction resource, marking transaction for rollback.", e);
                canCommit = false;
            }

            if (!canCommit)
            {
                break;
            }
        }

        if (canCommit)
        {
            for (TransactionResource resource : currentTransaction.getResources())
            {
                resource.commit();
            }

            currentTransaction.setStatus(TransactionStatus.COMMITTED);

            transactionHolder.set(null);
            activeTransaction.unlock();
        }
        else
        {
            rollback();

            //Question: indicate the failure to commit by throwing an exception?
        }

        for (Synchronization synchronization : currentTransaction.getSynchronizations())
        {
            synchronization.postCompletion(currentTransaction.getStatus());
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
            throw new TransactionException("No active transaction available.");
        }
        
        // check the txn depth.
        if (currentTransaction.getDepth() > 0)
        {
            currentTransaction.setDepth(currentTransaction.getDepth() - 1);
            currentTransaction.setStatus(TransactionStatus.ROLLBACKONLY);
            return;
        }

        currentTransaction.setStatus(TransactionStatus.ROLLINGBACK);

        for (TransactionResource resource : currentTransaction.getResources())
        {
            resource.rollback();
        }

        currentTransaction.setStatus(TransactionStatus.ROLLEDBACK);

        transactionHolder.set(null);
        activeTransaction.unlock();

        for (Synchronization synchronization : currentTransaction.getSynchronizations())
        {
            synchronization.postCompletion(currentTransaction.getStatus());
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
            throw new TransactionException("No active transaction available.");
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
            throw new TransactionException("No active transaction available.");
        }
        return activeTransaction.getStatus();
    }

    public Object runInTransaction(Executable action, TransactionResource... resources)
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
                Object result = action.execute();

                // execute a manual transaction.
                commit();

                return result;
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
                return action.execute();
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

    public static interface Executable
    {
        Object execute();
    }
}
