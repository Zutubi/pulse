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

import java.util.HashMap;
import java.util.Map;

public class PathPatternRecordLocatorTest extends ZutubiTestCase
{
    private RecordManager recordManager;

    private static final Record RECORD_A = new MutableRecordImpl();
    private static final Record RECORD_B = new MutableRecordImpl();

    protected void setUp() throws Exception
    {
        super.setUp();
        recordManager = mock(RecordManager.class);
    }

    public void testSinglePath()
    {
        doReturn(createMockResult("path", RECORD_A)).when(recordManager).selectAll("path");

        PathPatternRecordLocator locator = new PathPatternRecordLocator("path");
        Map<String, Record> result = locator.locate(recordManager);
        assertEquals(1, result.size());
        assertEquals(RECORD_A, result.get("path"));
    }

    public void testMultiplePaths()
    {
        doReturn(createMockResult("path", RECORD_A)).when(recordManager).selectAll("path");
        doReturn(createMockResult("otherpath", RECORD_B)).when(recordManager).selectAll("otherpath");

        PathPatternRecordLocator locator = new PathPatternRecordLocator("path", "otherpath");
        Map<String, Record> result = locator.locate(recordManager);
        assertEquals(2, result.size());
        assertEquals(RECORD_A, result.get("path"));
        assertEquals(RECORD_B, result.get("otherpath"));
    }

    public void testOverlappingPaths()
    {
        doReturn(createMockResult("path/blah", RECORD_A)).when(recordManager).selectAll("path/*");
        doReturn(createMockResult("path/blah", RECORD_A)).when(recordManager).selectAll("path/blah");

        PathPatternRecordLocator locator = new PathPatternRecordLocator("path/*", "path/blah");
        Map<String, Record> result = locator.locate(recordManager);
        assertEquals(1, result.size());
        assertEquals(RECORD_A, result.get("path/blah"));
    }

    private Map<String, Record> createMockResult(String path, Record record)
    {
        Map<String, Record> mockResult = new HashMap<String, Record>();
        mockResult.put(path, record);
        return mockResult;
    }
}
