package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;

public class NonScrubbedSimpleValueProblemTest extends AbstractHealthProblemTestCase
{
    private static final String PATH = "top";
    private static final String KEY = "key";
    private static final String VALUE = "inherited";
    
    private NonScrubbedSimpleValueProblem problem;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        problem = new NonScrubbedSimpleValueProblem(PATH, "message", KEY, VALUE);
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

    public void testKeyIsNotSimple()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        String nestedPath = PathUtils.getPath(PATH, KEY);
        recordManager.insert(nestedPath, new MutableRecordImpl());
        problem.solve(recordManager);
        
        assertTrue(recordManager.containsRecord(nestedPath));
    }

    public void testValueIsNotEqual()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put(KEY, "other");
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        Record after = recordManager.select(PATH);
        assertEquals("other", after.get(KEY));
    }

    public void testScrubs()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put(KEY, VALUE);
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        Record after = recordManager.select(PATH);
        assertFalse(after.containsKey(KEY));
    }
}