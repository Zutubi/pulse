package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;

public class UnexpectedHiddenKeysProblemTest extends AbstractHealthProblemTestCase
{
    private static final String PATH = "top";
    
    private UnexpectedHiddenKeysProblem problem;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        problem = new UnexpectedHiddenKeysProblem(PATH, "message");
    }

    public void testRecordDoesNotExist()
    {
        problem.solve(recordManager);
    }
    
    public void testNoHiddenKeys()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        problem.solve(recordManager);
    }

    public void testRemovesHiddenKeys()
    {
        MutableRecord record = new MutableRecordImpl();
        TemplateRecord.hideItem(record, "any");
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        Record after = recordManager.select(PATH);
        assertEquals(0, TemplateRecord.getHiddenKeys(after).size());
    }
}