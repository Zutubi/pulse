package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.ResourceProperty;
import com.zutubi.pulse.test.PulseTestCase;

import java.util.Map;

/**
 */
public class ScopeTest extends PulseTestCase
{
    private Scope parent;
    private Scope scope;

    protected void setUp() throws Exception
    {
        super.setUp();
        parent = new Scope();
        scope = new Scope(parent);
        parent.add(new Property("parent only", "parent"));
        parent.add(new Property("parent and child", "parent"));
        parent.add(new ResourceProperty("parent only resource", "parent resource", true, true));
        parent.add(new ResourceProperty("parent and child resource", "parent resource", true, true));

        scope.add(new Property("child only", "child"));
        scope.add(new Property("parent and child", "child"));
        scope.add(new ResourceProperty("child only resource", "child resource", true, true));
        scope.add(new ResourceProperty("parent and child resource", "child resource", true, true));

        scope.add(new ResourceProperty("not added", "not added", false, false));
    }

    public void testProperty()
    {
        assertEquals("child", getValue("child only"));
    }

    public void testOverriddenProperty()
    {
        assertEquals("child", getValue("parent and child"));
    }

    public void testEnvironment()
    {
        Map<String, String> environment = scope.getEnvironment();
        assertEquals(3, environment.size());
        assertEquals("parent resource", environment.get("parent only resource"));
        assertEquals("child resource", environment.get("child only resource"));
        assertEquals("child resource", environment.get("parent and child resource"));
    }

    public void testPath()
    {
        Map<String, String> paths = scope.getPathDirectories();
        assertEquals(3, paths.size());
        assertEquals("parent resource", paths.get("parent only resource"));
        assertEquals("child resource", paths.get("child only resource"));
        assertEquals("child resource", paths.get("parent and child resource"));
    }

    private String getValue(String name)
    {
        Reference reference = scope.getReference(name);
        return (String) reference.getValue();
    }
}
