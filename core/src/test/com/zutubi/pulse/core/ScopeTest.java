package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 */
public class ScopeTest extends PulseTestCase
{
    private Scope scope;

    protected void setUp() throws Exception
    {
        super.setUp();
        Scope parent = new Scope();
        scope = new Scope(parent);
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
        Scope s = new Scope();
        s.add(new ResourceProperty("test", "value", false, false, false));
        s.add(new ResourceProperty("test2", "${test}2", false, false, true));
        assertEquals("value2", s.getReference("test2").getValue());
    }

    public void testDontResolve()
    {
        Scope s = new Scope();
        s.add(new ResourceProperty("test", "value", false, false, false));
        s.add(new ResourceProperty("test2", "${test}2", false, false, false));
        assertEquals("${test}2", s.getReference("test2").getValue());
    }

    public void testSelfReference()
    {
        Scope s = new Scope();
        s.add(new ResourceProperty("testvar", "${testvar}", false, false, true));
        assertEquals("${testvar}", s.getReference("testvar").getValue());
    }

    public void testReferToParentProperty()
    {
        Scope p = new Scope();
        Scope c = new Scope(p);
        p.add(new ResourceProperty("test", "value", false, false, false));
        c.add(new ResourceProperty("test2", "${test}2", false, false, true));
        assertEquals("value2", c.getReference("test2").getValue());
    }

    public void testAddToPathPreservesOrder()
    {
        Scope s = new Scope();
        s.add(new ResourceProperty("j", "jv", false, true, false));
        s.add(new ResourceProperty("z", "zv", false, true, false));
        s.add(new ResourceProperty("a", "av", false, true, false));
        assertEquals(FileSystemUtils.composeSearchPath("av", "zv", "jv") + File.pathSeparatorChar, s.getPathPrefix());
    }

    public void testChildHidesParentAddToPath()
    {
        Scope p = new Scope();
        Scope c = new Scope(p);
        p.add(new ResourceProperty("testvar", "parent", false, true, false));
        c.add(new ResourceProperty("testvar", "child", false, false, false));
        assertEquals("parent" + File.pathSeparator, p.getPathPrefix());
        assertEquals("", c.getPathPrefix());
    }

    public void testChildHidesParentAddToEnv()
    {
        Scope p = new Scope();
        Scope c = new Scope(p);
        p.add(new ResourceProperty("priceless", "parent", true, false, false));
        c.add(new ResourceProperty("priceless", "child", false, false, false));
        assertFalse(c.containsReference("env.PRICELESS"));
    }

    public void testAddToEnvironmentAddsEnvVar()
    {
        Scope s = new Scope();
        s.add(new ResourceProperty("testvar", "value", true, false, false));
        assertEquals("value", s.getReference("env.testvar").getValue());
    }

    public void testAddToParentEnvAddsEnvVar()
    {
        Scope p = new Scope();
        Scope c = new Scope(p);
        p.add(new ResourceProperty("testvar", "value", true, false, false));
        assertEquals("value", c.getReference("env.testvar").getValue());
    }

    public void testAddToEnvReferenceEnvVar()
    {
        Scope s = new Scope();
        s.add(new ResourceProperty("testvar", "value", true, false, false));
        s.add(new ResourceProperty("testvar2", "${env.testvar}2", true, false, true));
        assertEquals("value2", s.getReference("testvar2").getValue());
    }

    public void testAddToParentEnvReferenceEnvVar()
    {
        Scope p = new Scope();
        Scope c = new Scope(p);
        p.add(new ResourceProperty("testvar", "value", true, false, false));
        c.add(new ResourceProperty("testvar2", "${env.testvar}2", true, false, true));
        assertEquals("value2", c.getReference("testvar2").getValue());
    }

    public void testReferenceEnvPath()
    {
        Scope s = new Scope();
        s.addEnvironmentProperty("PATH", "dummypath");
        s.add(new ResourceProperty("somevar", "someval", false, true, false));
        s.add(new ResourceProperty("refvar", "${env.PATH}?", false, false, true));
        assertEquals("someval" + File.pathSeparatorChar + "dummypath?", s.getReference("refvar").getValue());
    }

    public void testSelfReferenceEnvVar()
    {
        Scope s = new Scope();
        s.add(new ResourceProperty("refvar", "${env.refvar}?", true, false, true));
        assertEquals("${env.refvar}?", s.getReference("refvar").getValue());
    }

    public void testSelfReferenceEnvPath()
    {
        Scope s = new Scope();
        s.addEnvironmentProperty("PATH", "dummypath");
        s.add(new ResourceProperty("refvar", "${env.PATH}?", false, true, true));
        assertEquals("dummypath?", s.getReference("refvar").getValue());
    }

    public void testBackslash()
    {
        Scope s = new Scope();
        s.add(new ResourceProperty("myvar", "\\", false, true, true));
        assertEquals("\\", s.getReference("myvar").getValue());
    }

    public void testBadSyntax()
    {
        Scope s = new Scope();
        s.add(new ResourceProperty("myvar", "this ${ is invalid", false, true, true));
        assertEquals("this ${ is invalid", s.getReference("myvar").getValue());        
    }

    public void testGetReferencesIncludesParents()
    {
        Scope parent = new Scope();
        Scope child = new Scope(parent);

        parent.add(new ResourceProperty("name", "value"));

        List<Reference> references = child.getReferences();
        assertEquals(1, references.size());
        assertEquals("name", references.get(0).getName());
        assertEquals("value", references.get(0).getValue());
    }

    private String getValue(String name)
    {
        Reference reference = scope.getReference(name);
        return (String) reference.getValue();
    }
}
