package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;

import static com.zutubi.tove.type.record.PathUtils.getPath;

public class MissingCollectionProblemTest extends AbstractHealthProblemTestCase
{
    private static final String PATH = "foo";
    private static final String KEY = "key";
    
    private MissingCollectionProblem problem;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        problem = new MissingCollectionProblem(PATH, "message", KEY);
    }

    public void testRecordDoesNotExist()
    {
        problem.solve(recordManager);
    }
    
    public void testCollectionExists()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put(KEY, new MutableRecordImpl());
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        assertTrue(recordManager.containsRecord(getPath(PATH, KEY)));
    }

    public void testObstructedBySimpleKey()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put(KEY, "simple");
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        assertFalse(recordManager.containsRecord(getPath(PATH, KEY)));
    }
    
    public void testCollectionAdded()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        problem.solve(recordManager);
        
        assertTrue(recordManager.containsRecord(getPath(PATH, KEY)));        
    }
}