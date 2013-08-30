package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;

public class NonScrubbedMetaValueProblemTest extends AbstractHealthProblemTestCase
{
    private static final String PATH = "top";
    private static final String KEY = "key";
    private static final String VALUE = "inherited";
    
    private NonScrubbedMetaValueProblem problem;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        problem = new NonScrubbedMetaValueProblem(PATH, "message", KEY, VALUE);
    }

    public void testRecordDoesNotExist()
    {
        problem.solve(recordManager);
    }
    
    public void testKeyDoesNotExist()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        problem.solve(recordManager);
    }

    public void testValueIsNotEqual()
    {
        MutableRecord record = new MutableRecordImpl();
        record.putMeta(KEY, "other");
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        Record after = recordManager.select(PATH);
        assertEquals("other", after.getMeta(KEY));
    }

    public void testScrubs()
    {
        MutableRecord record = new MutableRecordImpl();
        record.putMeta(KEY, VALUE);
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        Record after = recordManager.select(PATH);
        assertFalse(after.containsMetaKey(KEY));
    }
}