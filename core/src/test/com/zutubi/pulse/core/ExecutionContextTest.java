package com.zutubi.pulse.core;

import com.zutubi.pulse.test.PulseTestCase;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 */
public class ExecutionContextTest extends PulseTestCase
{
    public void testInternalProperty()
    {
        ExecutionContext context = new ExecutionContext();
        context.addInternalString("foo", "bar");
        assertEquals("bar", context.getInternalString("foo"));
        assertEquals("bar", context.getString("foo"));
    }

    public void testUserProperty()
    {
        ExecutionContext context = new ExecutionContext();
        context.addString("foo", "bar");
        assertNull(context.getInternalString("foo"));
        assertEquals("bar", context.getString("foo"));
    }

    public void testPushPopInternalScope()
    {
        ExecutionContext context = new ExecutionContext();
        context.addInternalString("parent", "pv");
        context.pushInternalScope();
        context.addInternalString("child", "cv");
        assertEquals("pv", context.getInternalString("parent"));
        assertEquals("pv", context.getString("parent"));
        assertEquals("cv", context.getInternalString("child"));
        assertEquals("cv", context.getString("child"));
        context.popInternalScope();
        assertEquals("pv", context.getInternalString("parent"));
        assertEquals("pv", context.getString("parent"));
        assertNull(context.getInternalString("child"));
        assertNull(context.getString("child"));
    }

    public void testPushPopUserScope()
    {
        ExecutionContext context = new ExecutionContext();
        context.addString("parent", "pv");
        context.pushScope();
        context.addString("child", "cv");
        assertEquals("pv", context.getString("parent"));
        assertEquals("cv", context.getString("child"));
        context.popScope();
        assertEquals("pv", context.getString("parent"));
        assertNull(context.getString("child"));
    }

    public void testGetBooleanNotSet()
    {
        ExecutionContext context = new ExecutionContext();
        assertFalse(context.getBoolean("foo", false));
        assertTrue(context.getBoolean("foo", true));
    }

    public void testGetBooleanBoolean()
    {
        ExecutionContext context = new ExecutionContext();
        context.addValue("foo", true);
        assertTrue(context.getBoolean("foo", false));
    }

    public void testGetBooleanString()
    {
        ExecutionContext context = new ExecutionContext();
        context.addValue("foo", "true");
        assertTrue(context.getBoolean("foo", false));
    }

    public void testGetBooleanOther()
    {
        ExecutionContext context = new ExecutionContext();
        context.addValue("foo", new Object());
        assertFalse(context.getBoolean("foo", false));
    }

    public void testGetLongNotSet()
    {
        ExecutionContext context = new ExecutionContext();
        assertEquals(0L, context.getLong("foo"));
    }

    public void testGetLongLong()
    {
        ExecutionContext context = new ExecutionContext();
        context.addValue("foo", 1L);
        assertEquals(1L, context.getLong("foo"));
    }

    public void testGetLongString()
    {
        ExecutionContext context = new ExecutionContext();
        context.addValue("foo", "33");
        assertEquals(33L, context.getLong("foo"));
    }

    public void testGetLongUnparseableString()
    {
        ExecutionContext context = new ExecutionContext();
        context.addValue("foo", "eek");
        assertEquals(0L, context.getLong("foo"));
    }

    public void testGetLongOther()
    {
        ExecutionContext context = new ExecutionContext();
        context.addValue("foo", new Object());
        assertEquals(0L, context.getLong("foo"));
    }

    public void testGetFileNotSet()
    {
        ExecutionContext context = new ExecutionContext();
        assertNull(context.getFile("foo"));
    }

    public void testGetFileFile()
    {
        ExecutionContext context = new ExecutionContext();
        context.addValue("foo", new File("abc"));
        assertEquals(new File("abc"), context.getFile("foo"));
    }

    public void testGetFileString()
    {
        ExecutionContext context = new ExecutionContext();
        context.addValue("foo", "xyz");
        assertEquals(new File("xyz"), context.getFile("foo"));
    }

    public void testGetFileOther()
    {
        ExecutionContext context = new ExecutionContext();
        context.addValue("foo", new Object());
        assertNull(context.getFile("foo"));
    }

    public void testCopy()
    {
        ExecutionContext context = makeNonTrivialContext();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        context.setOutputStream(outputStream);
        context.setVersion("ver");
        context.setWorkingDir(new File("foo"));

        ExecutionContext copy = new ExecutionContext(context);
        assertEquals("ip", copy.getInternalString("iparent"));
        assertEquals("ic", copy.getInternalString("ichild"));
        assertEquals("p", copy.getString("parent"));
        assertEquals("c", copy.getString("child"));
        assertSame(outputStream, context.getOutputStream());
        assertEquals("ver", copy.getVersion());
        assertEquals(new File("foo"), copy.getWorkingDir());
    }

    public void testAsScope()
    {
        ExecutionContext context = makeNonTrivialContext();
        
        PulseScope scope = context.asScope();
        assertEquals("ip", scope.getReferenceValue("iparent", String.class));
        assertEquals("ic", scope.getReferenceValue("ichild", String.class));
        assertEquals("p", scope.getReferenceValue("parent", String.class));
        assertEquals("c", scope.getReferenceValue("child", String.class));
    }

    private ExecutionContext makeNonTrivialContext()
    {
        ExecutionContext context = new ExecutionContext();
        context.addInternalString("iparent", "ip");
        context.pushInternalScope();
        context.addInternalString("ichild", "ic");
        context.addString("parent", "p");
        context.pushScope();
        context.addString("child", "c");
        return context;
    }
}

