package com.zutubi.tove.transaction;

/**
 * A transaction resource is a resource that wants to participate in a transaction.  A
 * participating resource will be notified when it should 'prepare' for a transaction commit,
 * 'commit' its local changes or 'rollback' those local changes.
 */
public interface TransactionResource
{
    /**
     * This method is called prior to a commit, and indicates to this resource that it should do
     * everything it needs to do to ensure that when commit is called, that commit will be
     * successful.  It is <b>critical</b> that if this method returns true, a subsequent commit
     * will be successful.
     *
     * @return true if the local changes can be committed, false otherwise.
     */
    boolean prepare();

    /**
     * Commit the changes to the local resource.  Once committed, these changes will become
     * available to all threads, not just the thread making the changes.
     */
    void commit();

    /**
     * Rollback the changes to the local resource, leaving other threads non the wiser that anything
     * has changed.
     */
    void rollback();
}
