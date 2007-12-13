package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 */
public class ScopeTest extends PulseTestCase
{
    private PulseScope scope;

    protected void setUp() throws Exception
    {
        super.setUp();
        PulseScope parent = new PulseScope();
        scope = new PulseScope(parent);
        parent.add(new Property("parent only", "parent"));
        parent.add(new Property("parent and child", "parent"));
        parent.add(new ResourceProperty("parent only resource", "parent resource", true, true, false));
        parent.add(new ResourceProperty("parent and child resource", "parent resource", true, true, false));

        Map<String, String> env = System.getenv();
        for(Map.Entry<String, String> var: env.entrySet())
        {
            parent.addEnvironmentProperty(var.getKey(), var.getValue());
        }

        scope.add(new Property("child only", "child"));
        scope.add(new Property("parent and child", "child"));
        scope.add(new ResourceProperty("child only resource", "child resource", true, true, false));
        scope.add(new ResourceProperty("parent and child resource", "child resource", true, true, false));

        scope.add(new ResourceProperty("not added", "not added", false, false, false));
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
        List<String> paths = scope.getPathDirectories();
        assertEquals(3, paths.size());
        assertEquals("child resource", paths.get(0));
        assertEquals("child resource", paths.get(1));
        assertEquals("parent resource", paths.get(2));
    }

    public void testEnvPath()
    {
        Reference reference = scope.getReference("env.PATH");
        assertNotNull(reference);
        assertEquals(scope.getPathPrefix() + System.getenv("PATH"), reference.getValue());
    }

    public void testReferToEarlierProperty()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("test", "value", false, false, false));
        s.add(new ResourceProperty("test2", "${test}2", false, false, true));
        assertEquals("value2", s.getReference("test2").getValue());
    }

    public void testDontResolve()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("test", "value", false, false, false));
        s.add(new ResourceProperty("test2", "${test}2", false, false, false));
        assertEquals("${test}2", s.getReference("test2").getValue());
    }

    public void testSelfReference()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("testvar", "${testvar}", false, false, true));
        assertEquals("${testvar}", s.getReference("testvar").getValue());
    }

    public void testReferToParentProperty()
    {
        PulseScope p = new PulseScope();
        PulseScope c = new PulseScope(p);
        p.add(new ResourceProperty("test", "value", false, false, false));
        c.add(new ResourceProperty("test2", "${test}2", false, false, true));
        assertEquals("value2", c.getReference("test2").getValue());
    }

    public void testAddToPathPreservesOrder()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("j", "jv", false, true, false));
        s.add(new ResourceProperty("z", "zv", false, true, false));
        s.add(new ResourceProperty("a", "av", false, true, false));
        assertEquals(FileSystemUtils.composeSearchPath("av", "zv", "jv") + File.pathSeparatorChar, s.getPathPrefix());
    }

    public void testChildHidesParentAddToPath()
    {
        PulseScope p = new PulseScope();
        PulseScope c = new PulseScope(p);
        p.add(new ResourceProperty("testvar", "parent", false, true, false));
        c.add(new ResourceProperty("testvar", "child", false, false, false));
        assertEquals("parent" + File.pathSeparator, p.getPathPrefix());
        assertEquals("", c.getPathPrefix());
    }

    public void testChildHidesParentAddToEnv()
    {
        PulseScope p = new PulseScope();
        PulseScope c = new PulseScope(p);
        p.add(new ResourceProperty("priceless", "parent", true, false, false));
        c.add(new ResourceProperty("priceless", "child", false, false, false));
        assertFalse(c.containsReference("env.PRICELESS"));
    }

    public void testAddToEnvironmentAddsEnvVar()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("testvar", "value", true, false, false));
        assertEquals("value", s.getReference("env.testvar").getValue());
    }

    public void testAddToParentEnvAddsEnvVar()
    {
        PulseScope p = new PulseScope();
        PulseScope c = new PulseScope(p);
        p.add(new ResourceProperty("testvar", "value", true, false, false));
        assertEquals("value", c.getReference("env.testvar").getValue());
    }

    public void testAddToEnvReferenceEnvVar()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("testvar", "value", true, false, false));
        s.add(new ResourceProperty("testvar2", "${env.testvar}2", true, false, true));
        assertEquals("value2", s.getReference("testvar2").getValue());
    }

    public void testAddToParentEnvReferenceEnvVar()
    {
        PulseScope p = new PulseScope();
        PulseScope c = new PulseScope(p);
        p.add(new ResourceProperty("testvar", "value", true, false, false));
        c.add(new ResourceProperty("testvar2", "${env.testvar}2", true, false, true));
        assertEquals("value2", c.getReference("testvar2").getValue());
    }

    public void testReferenceEnvPath()
    {
        PulseScope s = new PulseScope();
        s.addEnvironmentProperty("PATH", "dummypath");
        s.add(new ResourceProperty("somevar", "someval", false, true, false));
        s.add(new ResourceProperty("refvar", "${env.PATH}?", false, false, true));
        assertEquals("someval" + File.pathSeparatorChar + "dummypath?", s.getReference("refvar").getValue());
    }

    public void testSelfReferenceEnvVar()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("refvar", "${env.refvar}?", true, false, true));
        assertEquals("${env.refvar}?", s.getReference("refvar").getValue());
    }

    public void testSelfReferenceEnvPath()
    {
        PulseScope s = new PulseScope();
        s.addEnvironmentProperty("PATH", "dummypath");
        s.add(new ResourceProperty("refvar", "${env.PATH}?", false, true, true));
        assertEquals("dummypath?", s.getReference("refvar").getValue());
    }

    public void testBackslash()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("myvar", "\\", false, true, true));
        assertEquals("\\", s.getReference("myvar").getValue());
    }

    public void testBadSyntax()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("myvar", "this ${ is invalid", false, true, true));
        assertEquals("this ${ is invalid", s.getReference("myvar").getValue());        
    }

    public void testGetReferencesIncludesParents()
    {
        PulseScope parent = new PulseScope();
        PulseScope child = new PulseScope(parent);

        parent.add(new ResourceProperty("name", "value"));

        Collection<Reference> references = child.getOldrefs();
        assertEquals(1, references.size());
        Reference reference = references.iterator().next();
        assertEquals("name", reference.getName());
        assertEquals("value", reference.getValue());
    }

    public void testCopy()
    {
        PulseScope original = new PulseScope();
        original.add(new Property("foo", "bar"));

        PulseScope copy = original.copy();
        assertEquals("bar", original.getReferenceValue("foo", String.class));
        assertEquals("bar", copy.getReferenceValue("foo", String.class));
        
        original.add(new Property("foo", "baz"));
        assertEquals("baz", original.getReferenceValue("foo", String.class));
        assertEquals("bar", copy.getReferenceValue("foo", String.class));
    }

    public void testCopyWithParent()
    {
        PulseScope parent = new PulseScope();
        parent.add(new Property("foo", "bar"));
        PulseScope original = new PulseScope(parent);

        PulseScope copy = original.copy();
        assertEquals("bar", parent.getReferenceValue("foo", String.class));
        assertEquals("bar", original.getReferenceValue("foo", String.class));
        assertEquals("bar", copy.getReferenceValue("foo", String.class));

        parent.add(new Property("foo", "baz"));
        assertEquals("baz", parent.getReferenceValue("foo", String.class));
        assertEquals("baz", original.getReferenceValue("foo", String.class));
        assertEquals("bar", copy.getReferenceValue("foo", String.class));
    }

    private String getValue(String name)
    {
        Reference reference = scope.getReference(name);
        return (String) reference.getValue();
    }
}
