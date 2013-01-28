package com.zutubi.tove.config.health;

import com.google.common.collect.Sets;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.TemplateRecord;

public class InvalidHiddenKeyProblemTest extends AbstractHealthProblemTestCase
{
    private static final String PATH = "top";
    private static final String KEY = "key";
    
    private InvalidHiddenKeyProblem problem;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        problem = new InvalidHiddenKeyProblem(PATH, "message", KEY);
    }

    public void testRecordDoesNotExist()
    {
        problem.solve(recordManager);
    }
    
    public void testNoHiddenKeys()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        problem.solve(recordManager);
        
        assertNull(recordManager.select(PATH).getMeta(TemplateRecord.HIDDEN_KEY));
    }

    public void testKeyNotHidden()
    {
        MutableRecord record = new MutableRecordImpl();
        TemplateRecord.hideItem(record, "other");
        recordManager.insert(PATH, record);
        problem.solve(recordManager);

        assertEquals(Sets.newHashSet("other"), TemplateRecord.getHiddenKeys(recordManager.select(PATH)));
    }

    public void testKeyRemoved()
    {
        MutableRecord record = new MutableRecordImpl();
        TemplateRecord.hideItem(record, KEY);
        TemplateRecord.hideItem(record, "other");
        recordManager.insert(PATH, record);
        problem.solve(recordManager);

        assertEquals(Sets.newHashSet("other"), TemplateRecord.getHiddenKeys(recordManager.select(PATH)));
    }

    public void testMultipleKeysRemoved()
    {
        MutableRecord record = new MutableRecordImpl();
        record.putMeta(TemplateRecord.HIDDEN_KEY, "other," + KEY + ",another," + KEY);
        recordManager.insert(PATH, record);
        problem.solve(recordManager);

        assertEquals(Sets.newHashSet("other", "another"), TemplateRecord.getHiddenKeys(recordManager.select(PATH)));
    }
}