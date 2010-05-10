package com.zutubi.tove.transaction.inmemory;

import com.zutubi.tove.transaction.AbstractTransactionTestCase;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.transaction.UserTransaction;

import java.util.Map;
import java.util.HashMap;

public class InMemoryTransactionResourceTest extends AbstractTransactionTestCase
{
    private InMemoryTransactionResource<Map<String, String>> resource = null;
    private UserTransaction txn;
    private TestInMemoryMapStateWrapper stateWrapper;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        TransactionManager transactionManager = new TransactionManager();

        txn = new UserTransaction(transactionManager);

        stateWrapper = new TestInMemoryMapStateWrapper();

        resource = new InMemoryTransactionResource<Map<String, String>>(stateWrapper);
        resource.setTransactionManager(transactionManager);
    }

    public void testCommit()
    {
        assertNull(resource.get(false).get("key"));

        txn.begin();

        resource.get(true).put("key", "value");

        txn.commit();

        assertNotNull(resource.get(false).get("key"));
    }

    public void testRollback()
    {
        assertNull(resource.get(false).get("key"));

        txn.begin();

        resource.get(true).put("key", "value");

        txn.rollback();

        assertNull(resource.get(false).get("key"));
    }

    public void testChangesDuringTranscationAreIsolated()
    {
        assertNull(resource.get(false).get("key"));

        txn.begin();

        resource.get(true).put("key", "value");

        executeOnSeparateThreadAndWait(new Runnable()
        {
            public void run()
            {
                assertNull(resource.get(false).get("key"));
            }
        });

        txn.commit();

        executeOnSeparateThreadAndWait(new Runnable()
        {
            public void run()
            {
                assertNotNull(resource.get(false).get("key"));
            }
        });
    }

    public void testCopyOnlyOnWrite()
    {
        txn.begin();
        assertFalse(stateWrapper.isCopyCalled());
        resource.get(false);
        assertFalse(stateWrapper.isCopyCalled());
        resource.get(true);
        assertTrue(stateWrapper.isCopyCalled());
        txn.commit();
    }

    private class TestInMemoryMapStateWrapper extends  InMemoryMapStateWrapper<String, String>
    {
        private boolean copyCalled = false;

        private TestInMemoryMapStateWrapper()
        {
            super(new HashMap<String, String>());
        }

        @Override
        protected InMemoryStateWrapper<Map<String, String>> copy()
        {
            copyCalled = true;
            return super.copy();
        }

        public boolean isCopyCalled()
        {
            return copyCalled;
        }
    }
}
