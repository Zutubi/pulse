/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

        assertEquals("value", resource.get(false).get("key"));
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
                assertEquals("value", resource.get(false).get("key"));
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
