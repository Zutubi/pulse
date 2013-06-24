package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;

import static com.zutubi.tove.type.record.PathUtils.getPath;

public class SimpleOverrideOfComplexProblemTest extends AbstractHealthProblemTestCase
{
    private static final String PARENT_PATH = "parent";
    private static final String PATH = "child";
    private static final String KEY = "key";
    
    private SimpleOverrideOfComplexProblem problem;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        problem = new SimpleOverrideOfComplexProblem(PATH, "message", KEY, PARENT_PATH);
    }

    public void testParentRecordDoesNotExist()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        problem.solve(recordManager);
    }

    public void testParentRecordDoesNotContainKey()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        recordManager.insert(PARENT_PATH, new MutableRecordImpl());
        problem.solve(recordManager);
    }

    public void testParentRecordHasSimpleKey()
    {
        MutableRecord parent = new MutableRecordImpl();
        parent.put(KEY, "simple");
        recordManager.insert(PARENT_PATH, parent);

        MutableRecord child = new MutableRecordImpl();
        child.put(KEY, "othersimple");
        recordManager.insert(PATH, child);

        problem.solve(recordManager);

        Record after = recordManager.select(PATH);
        assertNotNull(after);
        assertEquals("othersimple", after.get(KEY));
    }
    
    public void testContainingRecordDoesNotExist()
    {
        MutableRecord parent = new MutableRecordImpl();
        parent.put(KEY, new MutableRecordImpl());
        recordManager.insert(PARENT_PATH, parent);
        problem.solve(recordManager);
    }

    public void testChildRecordExists()
    {
        MutableRecord parent = new MutableRecordImpl();
        parent.put(KEY, new MutableRecordImpl());
        recordManager.insert(PARENT_PATH, parent);

        MutableRecord child = new MutableRecordImpl();
        MutableRecord skeleton = new MutableRecordImpl();
        skeleton.put("foo", "bar");
        child.put(KEY, skeleton);
        recordManager.insert(PATH, child);
        
        problem.solve(recordManager);
        
        Record skeletonAfter = recordManager.select(getPath(PATH, KEY));
        assertNotNull(skeletonAfter);
        assertEquals("bar", skeletonAfter.get("foo"));
    }

    public void testReplaceWithSkeleton()
    {
        MutableRecord parent = new MutableRecordImpl();
        MutableRecord inherited = new MutableRecordImpl();
        inherited.put("simple", "value");
        inherited.put("nested", new MutableRecordImpl());
        parent.put(KEY, inherited);
        recordManager.insert(PARENT_PATH, parent);

        MutableRecordImpl child = new MutableRecordImpl();
        child.put(KEY, "value");
        recordManager.insert(PATH, child);
        problem.solve(recordManager);
        
        Record skeleton = recordManager.select(getPath(PATH, KEY));
        assertNotNull(skeleton);
        assertNull(skeleton.get("simple"));
        Object nested = skeleton.get("nested");
        assertNotNull(nested);
        assertTrue(nested instanceof Record);
    }
}