package com.zutubi.tove.transaction;

/**
 * The possible status's of a transaction. 
 */
public enum TransactionStatus
{
    /**
     * The initial state of a transaction.
     */
    INACTIVE,

    /**
     * An active transaction is in progress.
     */
    ACTIVE,

    /**
     * The transaction has begun processing a commit.
     */
    COMMITTING,

    /**
     * The transaction has been successfully committed.
     */
    COMMITTED,

    /**
     * The transaction has been marked for rollback only.  This means that it will
     * not be committed.
     */
    ROLLBACKONLY,

    /**
     * The transaction is in the process of being rolled back.
     */
    ROLLINGBACK,

    /**
     * The transaction has been successfully rolled back. 
     */
    ROLLEDBACK
}
