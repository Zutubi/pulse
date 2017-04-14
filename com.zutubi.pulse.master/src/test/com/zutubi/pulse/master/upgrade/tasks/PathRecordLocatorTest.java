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

package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.junit.ZutubiTestCase;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Map;

public class PathRecordLocatorTest extends ZutubiTestCase
{
    private static final String INVALID_PATH = "invalid";
    private static final String VALID_PATH   = "valid";
    private static final Record RECORD = new MutableRecordImpl();

    private RecordManager recordManager;

    protected void setUp() throws Exception
    {
        super.setUp();
        recordManager = mock(RecordManager.class);
        doReturn(null).when(recordManager).select(INVALID_PATH);
        doReturn(RECORD).when(recordManager).select(VALID_PATH);
    }

    public void testRecordExists()
    {
        PathRecordLocator locator = new PathRecordLocator(VALID_PATH);
        Map<String, Record> records = locator.locate(recordManager);
        assertEquals(1, records.size());
        assertTrue(records.containsKey(VALID_PATH));
        assertSame(RECORD, records.get(VALID_PATH));
    }

    public void testRecordDoesNotExist()
    {
        PathRecordLocator locator = new PathRecordLocator(INVALID_PATH);
        assertEquals(0, locator.locate(recordManager).size());
    }
}
