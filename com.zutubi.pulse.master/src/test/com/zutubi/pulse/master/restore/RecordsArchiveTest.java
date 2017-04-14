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

package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.store.RecordStore;
import static org.mockito.Mockito.*;

import java.io.File;

public class RecordsArchiveTest extends PulseTestCase
{
    private File tmp;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDirectory();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);
        tmp = null;

        super.tearDown();
    }

    public void testUnitEmptyStore() throws ArchiveException
    {
        assertRoundTrip(new MutableRecordImpl());
    }

    public void testSimpleData() throws ArchiveException
    {
        MutableRecordImpl base = new MutableRecordImpl();
        base.put("a", "b");
        base.put("1", "2");

        assertRoundTrip(base);
    }

    public void testNestedData() throws ArchiveException
    {
        MutableRecordImpl nested = new MutableRecordImpl();
        nested.put("1", "2");
        MutableRecordImpl base = new MutableRecordImpl();
        base.put("a", "b");
        base.put("nested", nested);

        assertRoundTrip(base);
    }

    private void assertRoundTrip(Record base) throws ArchiveException
    {
        RecordStore store = mock(RecordStore.class);
        stub(store.exportRecords()).toReturn(base);

        RecordsArchive archive = new RecordsArchive();
        archive.setRecordStore(store);

        archive.backup(tmp);

        assertTrue(new File(tmp, RecordsArchive.ARCHIVE_FILENAME).isFile());

        archive.restore(tmp);

        verify(store, times(1)).importRecords(eq(base));
    }
}
