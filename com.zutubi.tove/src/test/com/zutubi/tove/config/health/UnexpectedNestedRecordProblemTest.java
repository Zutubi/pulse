package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;

public class UnexpectedNestedRecordProblemTest extends AbstractHealthProblemTestCase
{
    private static final String PATH = "top";
    private static final String KEY = "key";
    
    private UnexpectedNestedRecordProblem problem;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        problem = new UnexpectedNestedRecordProblem(PATH, "message", KEY);
    }

    public void testContainingRecordDoesNotExist()
    {
        problem.solve(recordManager);
    }
    
    public void testNestedRecordDoesNotExist()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        problem.solve(recordManager);
    }

    public void testKeyIsSimple()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put(KEY, "value");
        recordManager.insert(PATH, record);
        problem.solve(recordManager);

        Record after = recordManager.select(PATH);
        assertEquals("value", after.get(KEY));
    }

    public void testNestedRecordRemoved()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        String nestedPath = PathUtils.getPath(PATH, KEY);
        recordManager.insert(nestedPath, new MutableRecordImpl());
        problem.solve(recordManager);
        
        assertFalse(recordManager.containsRecord(nestedPath));
    }
}