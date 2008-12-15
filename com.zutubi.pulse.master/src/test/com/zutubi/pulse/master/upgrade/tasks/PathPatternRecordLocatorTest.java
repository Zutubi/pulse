package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.MutableRecordImpl;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;

import java.util.Map;
import java.util.HashMap;

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

    private Map<String, Record> createMockResult(String path, Record record)
    {
        Map<String, Record> mockResult = new HashMap<String, Record>();
        mockResult.put(path, record);
        return mockResult;
    }
}
