package com.zutubi.prototype.format;

import junit.framework.TestCase;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.util.List;

/**
 *
 *
 */
public class DisplayTest extends TestCase
{
    private Display display;

    protected void setUp() throws Exception
    {
        super.setUp();

        display = new Display();
        display.setObjectFactory(new DefaultObjectFactory());
    }

    protected void tearDown() throws Exception
    {
        display = null;

        super.tearDown();
    }

    public void testGetDisplayFields()
    {
        List<String> fieldNames = display.getDisplayFields(SampleDisplay.class);
        assertNotNull(fieldNames);
        assertEquals(1, fieldNames.size());
        assertTrue(fieldNames.contains("name"));
    }

    public void testDisplayFormat() throws Exception
    {
        Object obj = display.format(SampleDisplay.class, "name", new T());
        assertNotNull(obj);
        assertEquals("hello world", obj);
    }

    public static class T
    {

    }

    public static class SampleDisplay
    {
        // display field method for 'name' field
        public String getName(T t)
        {
            return "hello world";
        }

        // NOT a display field method
        public String get(T t)
        {
            return null;
        }
    }
}
