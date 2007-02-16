package com.zutubi.prototype.type.record;

import junit.framework.TestCase;

/**
 *
 *
 */
public class RecordManagerTest extends TestCase
{

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testStore()
    {
        RecordManager recordManager = new RecordManager();
        recordManager.store("hello", new Record());
        recordManager.store("hello/world", new Record());

        Record hello = recordManager.load("hello");
        assertNotNull(hello.get("world"));

        recordManager.delete("hello/world");
        assertNull(hello.get("world"));
    }
}
