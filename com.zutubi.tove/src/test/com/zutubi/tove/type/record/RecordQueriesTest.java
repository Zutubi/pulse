package com.zutubi.tove.type.record;

import com.zutubi.util.junit.ZutubiTestCase;

import java.util.List;

public class RecordQueriesTest extends ZutubiTestCase
{
    private MutableRecord base;
    private RecordQueries queries;

    protected void setUp() throws Exception
    {
        super.setUp();

        base = new MutableRecordImpl();
        queries = new RecordQueries(base);
    }

    public void testSelectRecordsOnly()
    {
        addToBase("a", "b");
        assertNull(queries.select("a"));

        addToBase("c", new Object());
        assertNull(queries.select("c"));
    }

    public void testSelectMatchingPath()
    {
        addToBase("a", new MutableRecordImpl());
        assertNotNull(queries.select("a"));

        addToBase("a/b", new MutableRecordImpl());
        assertNotNull(queries.select("a/b"));

        addToBase("b/c/d/e", new MutableRecordImpl());
        assertNotNull(queries.select("b/c/d/e"));
    }

    public void testSelectMatchingPathInSelectPaths()
    {
        addToBase("a/b", new MutableRecordImpl());
        List<String> selectedPaths = queries.selectPaths("a/b");
        assertEquals(1, selectedPaths.size());
        assertEquals("a/b", selectedPaths.get(0));
    }

    public void testWildCardInSelectPaths()
    {
        addToBase("a", new MutableRecordImpl());
        addToBase("b", new MutableRecordImpl());
        addToBase("c", new MutableRecordImpl());
        addToBase("a/1", new MutableRecordImpl());
        addToBase("b/2", new MutableRecordImpl());
        addToBase("b/1/d", new MutableRecordImpl());
        addToBase("b/2/e", new MutableRecordImpl());
        addToBase("c/1/f", new MutableRecordImpl());
        addToBase("c/2/g", new MutableRecordImpl());

        assertEquals(3, queries.selectPaths("*").size());
        assertEquals(5, queries.selectPaths("*/*").size());
        assertEquals(4, queries.selectPaths("*/*/*").size());

        assertEquals(2, queries.selectPaths("b/*/*").size());
        assertEquals(2, queries.selectPaths("*/1/*").size());

        assertEquals(1, queries.selectPaths("*/*/g").size());
    }

    private void addToBase(String path, Object value)
    {
        MutableRecord parent = base;
        String[] pathElements = PathUtils.getPathElements(path);
        for (int i = 0; i < pathElements.length - 1; i++)
        {
            MutableRecord selection = (MutableRecord) parent.get(pathElements[i]);
            if (selection == null)
            {
                selection = new MutableRecordImpl();
                parent.put(pathElements[i], selection);
            }
            parent = selection;
        }
        parent.put(pathElements[pathElements.length - 1], value);
    }
}
