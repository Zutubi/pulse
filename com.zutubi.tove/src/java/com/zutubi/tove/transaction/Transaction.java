package com.zutubi.tove.transaction;

import java.util.LinkedList;
import java.util.List;

/**
 * The transaction objects allows for operations to be performed against a specific transaction.
 *  
 * A Transaction object is created corresponding to each global transaction creation. The Transaction object
 * can be used for resource enlistment, transaction completion and status query operations.
 */
public class Transaction
{
    /**
     * System transaction manager that manages this transaction. 
     */
    private TransactionManager transactionManager;

    /**
     * The list of transaction resources that are participating in this transaction.
     */
    private List<TransactionResource> resources;

    /**
     * List of synchronizations that are participating in this transaction.
     */
    private List<Synchronisation> synchronisations;

    /**
     * The transactions current status.
     */
    private TransactionStatus status = TransactionStatus.INACTIVE;

    /**
     * Transaction depth represents the nested depth of the transaction, and is used
     * to ensure that a transaction is not commited when nested within another transaction
     * begin - commit.
     */
    private int depth;

    /**
     * Transaction id, unique to all transactions within a particular transaction manager.
     */
    private long id;

    Transaction(long id, TransactionManager transactionManager)
    {
        this.id = id;
        this.transactionManager = transactionManager;
    }

    /**
     * Complete the transaction represented by this Transaction object
     */
    public void commit()
    {
        assertActiveTransaction();

        this.transactionManager.commit();
    }

    /**
     * Rollback the transaction represented by this Transaction object.
     */
    public void rollback()
    {
        assertActiveTransaction();

        this.transactionManager.rollback();
    }

    /**
     * Modify the transaction associated with the current thread such that the only possible outcome of
     * the transaction is to roll back the transaction.
     */
    public void setRollbackOnly()
    {
        this.transactionManager.setRollbackOnly();
    }

    /**
     * Obtain the status of the transaction associated with the current thread.
     *
     * @return status
     *
     * @see com.zutubi.tove.transaction.TransactionStatus
     */
    public TransactionStatus getStatus()
    {
        return status;
    }

    void setStatus(TransactionStatus status)
    {
        this.status = status;
    }

    /**
     * Register a transaction resource with this transaction.  Any registered resource will participate
     * commit / rollback processing of this transaction.  
     *
     * @param resource to be registered.
     */
    public void enlistResource(TransactionResource resource)
    {
        // can only enlist a resource if this transaction is considered active.
        assertActiveTransaction();
        
        if (!getResources().contains(resource))
        {
            getResources().add(resource);
        }
    }

    public void delistResource(TransactionResource resource)
    {
        // can only delist a resource if this transaction is considered active.
        assertActiveTransaction();

        getResources().remove(resource);
    }

    public void registerSynchronization(Synchronisation sync)
    {
        // can only register a synchronisation if this transaction is considered active.
        assertActiveTransaction();

        if (!getSynchronizations().contains(sync))
        {
            getSynchronizations().add(sync);
        }
    }

    List<Synchronisation> getSynchronizations()
    {
        if (synchronisations == null)
        {
            synchronisations = new LinkedList<Synchronisation>();
        }
        return synchronisations;
    }

    List<TransactionResource> getResources()
    {
        if (resources == null)
        {
            resources = new LinkedList<TransactionResource>();
        }
        return resources;
    }

    /**
     * Assert that this transaction is in an active state.
     */
    void assertActiveTransaction()
    {
        if (status != TransactionStatus.ACTIVE && status != TransactionStatus.ROLLBACKONLY)
        {
            throw new IllegalStateException("Invalid transaction status: " + status.toString().toLowerCase());
        }
    }

    void setDepth(int depth)
    {
        this.depth = depth;
    }

    int getDepth()
    {
        return depth;
    }

    public long getId()
    {
        return id;
    }
}
