package com.zutubi.tove.transaction;

/**
 * A simple callback handler that allows for a notification when a transaction is completed.
 */
public interface Synchronisation
{
    /**
     * Callback that is triggered when the transaction this synchronisation
     * is registered with completed.
     *
     * @param status    the status of the transaction.
     */
    void postCompletion(TransactionStatus status);
}
