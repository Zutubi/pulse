package com.zutubi.tove.transaction;

import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The transaction objects allows for operations to be performed against a specific transaction.
 *  
 * A Transaction object is created corresponding to each global transaction creation. The Transaction object
 * can be used for resource enlistment, transaction completion and status query operations.
 */
public class Transaction
{
    private static final Logger LOG = Logger.getLogger(Transaction.class);

    private static final String PROPERTY_TRANSACTION_TIME_WARNING_MILLIS = "pulse.transaction.time.warning.millis";
    private static final long DEFAULT_TRANSACTION_TIME_WARNING_MILLIS = 20000;

    /**
     * System transaction manager that manages this transaction. 
     */
    private TransactionManager transactionManager;

    /**
     * The list of transaction resources that are participating in this transaction.
     */
    private List<TransactionResource> resources;

    /**
     * List of synchronisations that are participating in this transaction.
     */
    private List<Synchronisation> synchronisations;

    /**
     * The transactions current status.
     */
    private TransactionStatus status = TransactionStatus.INACTIVE;

    private long activateTime;

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

    /**
     * A map of data bound to this transaction. 
     */
    private Map<String, Object> transactionalData;    

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
        switch(status)
        {
            case ACTIVE:
            {
                activateTime = System.currentTimeMillis();
                break;
            }
            case COMMITTED:
            case ROLLEDBACK:
            {
                long elapsedMillis = System.currentTimeMillis() - activateTime;
                if (elapsedMillis > Long.getLong(PROPERTY_TRANSACTION_TIME_WARNING_MILLIS, DEFAULT_TRANSACTION_TIME_WARNING_MILLIS))
                {
                    try
                    {
                        // Raise an exception so we get a full stack trace.
                        throw new RuntimeException("Transaction took more than " + (elapsedMillis / 1000) + " seconds");
                    }
                    catch (RuntimeException e)
                    {
                        LOG.warning(e.getMessage(), e);
                    }
                }
            }
        }

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

    public void registerSynchronisation(Synchronisation sync)
    {
        // can only register a synchronisation if this transaction is considered active.
        assertActiveTransaction();

        if (!getSynchronisations().contains(sync))
        {
            getSynchronisations().add(sync);
        }
    }

    /**
     * Bind the data to this transaction with the specified key.  This data
     * will be available during the scope of this transaction. 
     *
     * @param key   the key used to identify the data
     * @param data  the data being bound to this transaction.
     */
    public void put(String key, Object data)
    {
        assertActiveTransaction();

        getTransactionalData().put(key, data);
    }

    /**
     * Get specific data that has been bound to this transaction.
     * 
     * @param key   the key identifying the data to be retrieved.
     * @param <V>   the type of the data being retrieved.
     * @return  the requested data.
     */
    public <V> V get(String key)
    {
        // Assume that the client knows the type of the data they are requesting. 
        //noinspection unchecked
        return (V) getTransactionalData().get(key);
    }

    Map<String, Object> getTransactionalData()
    {
        if (transactionalData == null)
        {
            transactionalData = new HashMap<String, Object>();
        }
        return transactionalData;
    }

    List<Synchronisation> getSynchronisations()
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
