package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.collect.ImmutableMap;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.junit.ZutubiTestCase;
import org.mockito.Matchers;

import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class FirstDefinedFilterRecordLocatorTest extends ZutubiTestCase
{
    private FirstDefinedFilterRecordLocator locator;

    private static final String PATH_FIRST_DEFINED = "projects/parent/foo";
    private static final String PATH_INHERITED = "projects/child/foo";
    
    private static final Record RECORD_FIRST_DEFINED = new MutableRecordImpl();
    private static final Record RECORD_INHERITED = new MutableRecordImpl();

    protected void setUp() throws Exception
    {
        super.setUp();
        
        RecordLocator delegate = mock(RecordLocator.class);
        Map<String, Record> delegateResults = ImmutableMap.of(PATH_FIRST_DEFINED, RECORD_FIRST_DEFINED, PATH_INHERITED, RECORD_INHERITED);
        doReturn(delegateResults).when(delegate).locate(Matchers.<RecordManager>anyObject());
        
        TemplatedScopeDetails scope = mock(TemplatedScopeDetails.class);
        doReturn(false).when(scope).hasAncestor(PATH_FIRST_DEFINED);
        doReturn(true).when(scope).hasAncestor(PATH_INHERITED);
        
        locator = new FirstDefinedFilterRecordLocator(delegate, scope);
    }

    public void testFilter()
    {
        Map<String, Record> result = locator.locate(mock(RecordManager.class));
        assertEquals(1, result.size());
        assertEquals(RECORD_FIRST_DEFINED, result.get(PATH_FIRST_DEFINED));
    }
}