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

package com.zutubi.tove.type.record.store;

import com.zutubi.tove.transaction.TransactionManager;

import java.io.File;

public class FileSystemRecordStoreInterfaceTest extends AbstractRecordStoreInterfaceTestCase
{
    private File persistentDirectory;

    protected void setUp() throws Exception
    {
        persistentDirectory = createTempDirectory();

        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(persistentDirectory);
        super.tearDown();
    }

    public RecordStore createRecordStore() throws Exception
    {
        FileSystemRecordStore recordStore = new FileSystemRecordStore();
        recordStore.setTransactionManager(new TransactionManager());
        recordStore.setPersistenceDirectory(persistentDirectory);
        recordStore.init();
        return recordStore;
    }
}
