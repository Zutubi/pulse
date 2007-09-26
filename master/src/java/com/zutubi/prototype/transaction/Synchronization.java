package com.zutubi.prototype.transaction;

/**
 * A simple callback handler that allows for a notification when a transaction is completed.
 *
 */
public interface Synchronization
{
    void postCompletion(TransactionStatus status);
}
