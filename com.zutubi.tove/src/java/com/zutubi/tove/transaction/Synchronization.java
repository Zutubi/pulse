package com.zutubi.tove.transaction;

/**
 * A simple callback handler that allows for a notification when a transaction is completed.
 */
public interface Synchronization
{
    /**
     * Callback that is triggered when the transaction this synchronization
     * is registered with completed.
     *
     * @param status    the status of the transaction.
     */
    void postCompletion(TransactionStatus status);
}
