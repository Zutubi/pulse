package com.zutubi.tove.transaction;

public class TransactionManagerTest extends AbstractTransactionTestCase
{
    private TransactionManager transactionManager;
    private UserTransaction userTransaction;

    protected void setUp() throws Exception
    {
        super.setUp();

        transactionManager = new TransactionManager();
        userTransaction = new UserTransaction(transactionManager);
    }

    // check that the transaction ids are unique and sequentially increasing(?)
    public void testUniqueTransactionIdsAllocatedToTransactions()
    {
        userTransaction.begin();

        long id = transactionManager.getTransaction().getId();

        userTransaction.commit();
        userTransaction.begin();

        assertTrue(id < transactionManager.getTransaction().getId());
        id = transactionManager.getTransaction().getId();

        userTransaction.rollback();
        userTransaction.begin();

        assertTrue(id < transactionManager.getTransaction().getId());

        userTransaction.commit();
    }

    public void testSimpleCommitTransactionFlow()
    {
        userTransaction.begin();

        Transaction transaction = transactionManager.getTransaction();
        
        assertEquals(TransactionStatus.ACTIVE, userTransaction.getStatus());
        assertEquals(TransactionStatus.ACTIVE, transaction.getStatus());

        // do something here.
        TransactionalResource e = new TransactionalResource(transactionManager);
        e.interactWithMe();

        userTransaction.commit();

        assertEquals(TransactionStatus.COMMITTED, transaction.getStatus());
        assertTrue(e.isCommitted());
    }

    public void testSimpleRollbackTransactionFlow()
    {
        userTransaction.begin();

        Transaction transaction = transactionManager.getTransaction();

        assertEquals(TransactionStatus.ACTIVE, userTransaction.getStatus());
        assertEquals(TransactionStatus.ACTIVE, transaction.getStatus());

        // do something here.
        TransactionalResource resource = new TransactionalResource(transactionManager);
        resource.interactWithMe();

        userTransaction.rollback();

        assertEquals(TransactionStatus.ROLLEDBACK, transaction.getStatus());
        assertTrue(resource.isRolledback());
    }

    public void testRollbackDueToPrepareFailure()
    {
        userTransaction.begin();

        Transaction transaction = transactionManager.getTransaction();

        // do something here.
        TransactionalResource resource = new TransactionalResource(transactionManager);
        resource.interactWithMe();
        resource.setPreparationResponse(false);

        try
        {
            userTransaction.commit();
            fail();
        }
        catch (RollbackException e)
        {
            // expected.
        }

        assertEquals(TransactionStatus.ROLLEDBACK, transaction.getStatus());
        assertTrue(resource.isRolledback());
    }

    public void testRollbackDueToRollbackOnly()
    {
        userTransaction.begin();

        Transaction transaction = transactionManager.getTransaction();

        // do something here.
        TransactionalResource resource = new TransactionalResource(transactionManager);
        resource.interactWithMe();

        userTransaction.setRollbackOnly();
        userTransaction.commit();

        assertEquals(TransactionStatus.ROLLEDBACK, transaction.getStatus());
        assertTrue(resource.isRolledback());
    }

    public void testCommittingAnInactiveTransaction()
    {
        try
        {
            transactionManager.commit();
            fail();
        }
        catch (TransactionException e)
        {
            assertEquals("No active transaction available.", e.getMessage());
        }
    }

    public void testSyncTriggeredOnCommit()
    {
        userTransaction.begin();

        final TransactionStatus[] transactionStatus = new TransactionStatus[1];
        Transaction transaction = transactionManager.getTransaction();
        transaction.registerSynchronisation(new Synchronisation()
        {
            public void postCompletion(Transaction txn)
            {
                transactionStatus[0] = txn.getStatus();
            }
        });

        userTransaction.commit();

        assertEquals(TransactionStatus.COMMITTED, transactionStatus[0]);
    }

    public void testSyncTriggeredOnRollback()
    {
        userTransaction.begin();

        final TransactionStatus[] transactionStatus = new TransactionStatus[1];
        Transaction transaction = transactionManager.getTransaction();
        transaction.registerSynchronisation(new Synchronisation()
        {
            public void postCompletion(Transaction txn)
            {
                transactionStatus[0] = txn.getStatus();
            }
        });

        userTransaction.rollback();

        assertEquals(TransactionStatus.ROLLEDBACK, transactionStatus[0]);
    }

    public void testCommittedTransactionStatusWhenNoResourcesRegistered()
    {
        userTransaction.begin();

        Transaction transaction = transactionManager.getTransaction();

        userTransaction.commit();

        assertEquals(TransactionStatus.COMMITTED, transaction.getStatus());
    }

    public void testRolledbackTransactionStatusWhenNoResourcesRegistered()
    {
        userTransaction.begin();

        Transaction transaction = transactionManager.getTransaction();

        userTransaction.rollback();

        assertEquals(TransactionStatus.ROLLEDBACK, transaction.getStatus());
    }

    public void testSerialisationOfTransactions()
    {
        userTransaction.begin();

        final TransactionalResource e = new TransactionalResource(transactionManager);

        final UserTransaction[] internalTransaction = new UserTransaction[1];
        executeOnSeparateThread(new Runnable()
        {
            public void run()
            {
                UserTransaction userTransaction = new UserTransaction(transactionManager);
                internalTransaction[0] = userTransaction;

                userTransaction.begin();

                e.interactWithMe();

                userTransaction.commit();
                internalTransaction[0] = null;
            }
        });

        // Pause this thread whilst the separate thread is doing what it needs to.
        // A semaphore is awkward here since the userTransaction.begin is a blocking call.
        while (internalTransaction[0] == null)
        {
            Thread.yield();
        }

        assertFalse(e.isInteractionOccured());

        userTransaction.commit();

        // Pause while the separate thread completes.
        while (internalTransaction[0] != null)
        {
            Thread.yield();
        }

        assertTrue(e.isInteractionOccured());
    }

    public void testNestedTransaction()
    {
        userTransaction.begin();

        assertEquals(TransactionStatus.ACTIVE, userTransaction.getStatus());

        userTransaction.begin();

        // do something here.
        TransactionalResource e = new TransactionalResource(transactionManager);
        e.interactWithMe();

        userTransaction.commit();

        assertEquals(TransactionStatus.ACTIVE, userTransaction.getStatus());

        userTransaction.commit();

        assertTrue(e.isCommitted());
    }

    public void testNestedTransactionRollback()
    {
        userTransaction.begin();

        assertEquals(TransactionStatus.ACTIVE, userTransaction.getStatus());

        userTransaction.begin();

        // do something here.
        TransactionalResource e = new TransactionalResource(transactionManager);
        e.interactWithMe();

        userTransaction.rollback();

        assertEquals(TransactionStatus.ROLLBACKONLY, userTransaction.getStatus());

        userTransaction.commit();

        assertTrue(e.isRolledback());
    }

    public void testExceptionDuringTransactionTriggersRollback()
    {
        final TransactionalResource resource = new TransactionalResource(transactionManager);

        try
        {
            transactionManager.runInTransaction(new Runnable()
            {
                public void run()
                {
                    resource.interactWithMe();

                    throw new RuntimeException("badness");
                }
            });
            fail();
        }
        catch (Exception e)
        {
            // noop, expected.
        }
        assertTrue(resource.isRolledback());
    }

    public void testExceptionDuringNestedTransactionMarksRollbackOnly()
    {
        userTransaction.begin();

        final TransactionalResource resource = new TransactionalResource(transactionManager);

        try
        {
            transactionManager.runInTransaction(new Runnable()
            {
                public void run()
                {
                    resource.interactWithMe();

                    throw new RuntimeException("badness");
                }
            });
            fail();
        }
        catch (Exception e)
        {
            // noop, expected.
        }
        assertEquals(TransactionStatus.ROLLBACKONLY, userTransaction.getStatus());
        userTransaction.commit();
        assertTrue(resource.isRolledback());
    }

    public void testTransactionalData()
    {
        transactionManager.begin();
        Transaction txn = transactionManager.getTransaction();
        txn.put("key", "value");
        assertEquals("value", txn.get("key"));
        transactionManager.commit();
    }

    private class TransactionalResource implements TransactionResource
    {
        private TransactionManager transactionManager;

        private boolean prepared = false;
        private boolean committed = false;
        private boolean rolledback = false;
        private boolean interactionOccured = false;
        private boolean preparationResponse = true;

        public TransactionalResource(TransactionManager transactionManager)
        {
            this.transactionManager = transactionManager;
        }

        public void interactWithMe()
        {
            Transaction txn = this.transactionManager.getTransaction();
            txn.enlistResource(this);

            interactionOccured = true;
        }

        public void setPreparationResponse(boolean preparationResponse)
        {
            this.preparationResponse = preparationResponse;
        }

        public boolean prepare()
        {
            prepared = true;

            assertEquals(TransactionStatus.COMMITTING, this.transactionManager.getStatus());

            return preparationResponse;
        }

        public void commit()
        {
            if (!prepared)
            {
                fail("can not commit a resource before it has been prepared.");
            }
            committed = true;

            assertEquals(TransactionStatus.COMMITTING, this.transactionManager.getStatus());
        }

        public void rollback()
        {
            rolledback = true;

            assertEquals(TransactionStatus.ROLLINGBACK, this.transactionManager.getStatus());
        }

        public boolean isPrepared()
        {
            return prepared;
        }

        public boolean isCommitted()
        {
            return committed;
        }

        public boolean isRolledback()
        {
            return rolledback;
        }

        public boolean isInteractionOccured()
        {
            return interactionOccured;
        }
    }
}
