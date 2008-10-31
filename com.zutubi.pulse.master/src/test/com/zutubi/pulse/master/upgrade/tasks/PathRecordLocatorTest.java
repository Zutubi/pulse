package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import junit.framework.TestCase;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Map;

public class PathRecordLocatorTest extends TestCase
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
