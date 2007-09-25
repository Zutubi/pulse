package com.zutubi.prototype.transaction;

/**
 *
 *
 */
public interface Synchronization
{
    void postCompletion(TransactionStatus status);
}
