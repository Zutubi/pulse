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

package com.zutubi.tove.config.health;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.tove.type.record.store.InMemoryRecordStore;
import com.zutubi.util.junit.ZutubiTestCase;

public abstract class AbstractHealthProblemTestCase extends ZutubiTestCase
{
    protected RecordManager recordManager;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        TransactionManager transactionManager = new TransactionManager();

        InMemoryRecordStore recordStore = new InMemoryRecordStore();
        recordStore.setTransactionManager(transactionManager);
        recordStore.init();

        recordManager = new RecordManager();
        recordManager.setEventManager(new DefaultEventManager());
        recordManager.setRecordStore(recordStore);
        recordManager.setTransactionManager(transactionManager);
        recordManager.init();
    }
}
