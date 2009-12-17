package com.zutubi.tove.transaction;

import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 */
public class TransactionalWrapperTest extends AbstractTransactionTestCase
{
    private TransactionManager transactionManager;
    private FakeTransactionalWrapper wrapper;

    protected void setUp() throws Exception
    {
        super.setUp();

        transactionManager = new TransactionManager();

        wrapper = new FakeTransactionalWrapper(new HashSet<String>());
        wrapper.setTransactionManager(transactionManager);
    }

    protected void tearDown() throws Exception
    {
        wrapper = null;
        transactionManager = null;

        super.tearDown();
    }

    public void testExceptionDuringInternalTransactionTriggersRollback()
    {
        try
        {
            wrapper.execute(new TransactionalWrapper.Action<Set<String>>()
            {
                public Object execute(Set<String> strings)
                {
                    strings.add("fail");
                    throw new RuntimeException("boom.");
                }
            });
            // we expect an exception.
            fail();
        }
        catch (Exception e)
        {
            // noop, expected.
        }

        Set<String> set = wrapper.get();
        assertFalse(set.contains("fail"));
    }

    public void testExceptionDuringExistingTransactionTriggersRollback()
    {
        transactionManager.begin();

        try
        {
            wrapper.execute(new TransactionalWrapper.Action<Set<String>>()
            {
                public Object execute(Set<String> strings)
                {
                    strings.add("fail");
                    throw new RuntimeException("boom.");
                }
            });
            // we expect an exception.
            fail();
        }
        catch (Exception e)
        {
            // noop, expected.
        }

        transactionManager.commit();

        Set<String> set = wrapper.get();
        assertFalse(set.contains("fail"));
    }

    public void testRollbackUndoesChange()
    {
        transactionManager.begin();

        wrapper.addToSet("fail");

        transactionManager.rollback();

        Set<String> set = wrapper.get();
        assertFalse(set.contains("fail"));
    }

    public void testTransactionThreadIsolation()
    {
        transactionManager.begin();

        wrapper.addToSet("item");

        executeOnSeparateThreadAndWait(new Runnable()
        {
            public void run()
            {
                assertFalse(wrapper.get().contains("item"));
            }
        });

        transactionManager.commit();

        executeOnSeparateThreadAndWait(new Runnable()
        {
            public void run()
            {
                assertTrue(wrapper.get().contains("item"));
            }
        });
    }

    private class FakeTransactionalWrapper extends TransactionalWrapper<Set<String>>
    {
        public FakeTransactionalWrapper(Set<String> global)
        {
            super(global);
        }

        public Set<String> copy(Set<String> v)
        {
            return new HashSet<String>(v);
        }

        public void addToSet(final String str)
        {
            this.execute(new TransactionalWrapper.Action<Set<String>>()
            {
                public Object execute(Set<String> strings)
                {
                    strings.add(str);
                    return null;
                }
            });
        }
    }
}
