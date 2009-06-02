package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.Reference;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.test.EqualityAssertions;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.StringUtils;

import java.io.File;
import java.util.*;

/**
 */
public class PulseScopeTest extends PulseTestCase
{
    private PulseScope scope;

    protected void setUp() throws Exception
    {
        super.setUp();
        PulseScope parent = new PulseScope();
        parent.setLabel("parent");
        scope = new PulseScope(parent);
        scope.setLabel("child");
        parent.add(new GenericReference<String>("parent only", "parent"));
        parent.add(new GenericReference<String>("parent and child", "parent"));
        parent.add(new ResourceProperty("parent only resource", "parent resource", true, true, false));
        parent.add(new ResourceProperty("parent and child resource", "parent resource", true, true, false));

        Map<String, String> env = System.getenv();
        for(Map.Entry<String, String> var: env.entrySet())
        {
            parent.addEnvironmentProperty(var.getKey(), var.getValue());
        }

        scope.add(new GenericReference<String>("child only", "child"));
        scope.add(new GenericReference<String>("parent and child", "child"));
        scope.add(new ResourceProperty("child only resource", "child resource", true, true, false));
        scope.add(new ResourceProperty("parent and child resource", "child resource", true, true, false));

        scope.add(new ResourceProperty("not added", "not added", false, false, false));
    }

    public void testCreateChild()
    {
        PulseScope child = scope.createChild();
        assertEquals(scope.getClass(), child.getClass());
        assertSame(child.getParent(), scope);
    }

    public void testGetRoot()
    {
        assertSame(scope.getParent(), scope.getRoot());
    }
    
    public void testGetRootTrivial()
    {
        PulseScope trivial = new PulseScope();
        assertSame(trivial, trivial.getRoot());
    }

    public void testProperty()
    {
        assertEquals("child", getValue("child only"));
    }

    public void testOverriddenProperty()
    {
        assertEquals("child", getValue("parent and child"));
    }


    public void testAddUnique()
    {
        try
        {
            scope.addUnique(new GenericReference<String>("child only", ""));
            fail();
        }
        catch(IllegalArgumentException e)
        {
            assertEquals("'child only' is already defined in this scope.", e.getMessage());
        }
    }

    public void testAddUniqueOverride()
    {
        scope.addUnique(new GenericReference<String>("parent only", "override"));
        assertEquals("override", scope.getReferenceValue("parent only", String.class));
    }

    public void testAddAll()
    {
        scope.addAll(Arrays.asList(new GenericReference<String>("p1", "v1"), new GenericReference<String>("p2", "v2")));
        assertEquals("v1", scope.getReferenceValue("p1", String.class));
        assertEquals("v2", scope.getReferenceValue("p2", String.class));
    }

    public void testAddAllUnique()
    {
        try
        {
            scope.addAllUnique(Arrays.asList(new GenericReference<String>("child only", "")));
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("'child only' is already defined in this scope.", e.getMessage());
        }
    }

    public void testGetReferences()
    {
        PulseScope parent = new PulseScope();
        parent.add(new GenericReference<String>("parent", "pv"));
        parent.add(new GenericReference<String>("both", "bv"));
        PulseScope child = new PulseScope(parent);
        child.add(new GenericReference<String>("child", "cv"));
        child.add(new GenericReference<String>("both", "bv"));

        Collection<Reference> references = child.getReferences();
        assertEquals(3, references.size());
        EqualityAssertions.assertEquals(Arrays.asList("parent", "child", "both"), CollectionUtils.map(references, new Mapping<Reference, String>()
        {
            public String map(Reference reference)
            {
                return reference.getName();
            }
        }));
    }

    public void testGetReferencesOfType()
    {
        PulseScope scope = new PulseScope();
        scope.add(new GenericReference<Long>("long", 1L));
        scope.add(new GenericReference<String>("string", "s"));

        Collection<Reference> references = scope.getReferences(String.class);
        assertEquals(1, references.size());
        assertEquals("string", references.iterator().next().getName());
    }

    public void testGetReferencesOfTypeOverrideType()
    {
        PulseScope parent = new PulseScope();
        parent.add(new GenericReference<Long>("long then string", 1L));
        parent.add(new GenericReference<String>("string then long", "s"));

        PulseScope child = new PulseScope(parent);
        parent.add(new GenericReference<String>("long then string", "s"));
        parent.add(new GenericReference<Long>("string then long", 1L));
        
        Collection<Reference> references = child.getReferences(String.class);
        assertEquals(1, references.size());
        assertEquals("long then string", references.iterator().next().getName());
    }

    public void testGetReferenceValue()
    {
        assertEquals("child", scope.getReferenceValue("child only", String.class));
    }

    public void testGetReferenceValueWrongType()
    {
        assertNull(scope.getReferenceValue("child only", Long.class));
    }

    public void testEnvironment()
    {
        Map<String, String> environment = scope.getEnvironment();
        assertEnvironment(environment);
    }

    public void testApplyEnvironment()
    {
        Map<String, String> originalEnvironment = new HashMap<String, String>();
        originalEnvironment.put("foo", "bar");
        originalEnvironment.put("child only resource", "overridden");
        scope.applyEnvironment(originalEnvironment);
        assertEnvironment(originalEnvironment);
        assertEquals("bar", originalEnvironment.get("foo"));
    }

    private void assertEnvironment(Map<String, String> environment)
    {
        assertEquals("parent resource", environment.get("parent only resource"));
        assertEquals("child resource", environment.get("child only resource"));
        assertEquals("child resource", environment.get("parent and child resource"));
    }

    public void testApplyEnvironmentPath()
    {
        Map<String, String> env = new HashMap<String, String>(System.getenv());
        String pathKey = findPathKey(env);
        scope.applyEnvironment(env);
        assertEquals(scope.getPathPrefix() + System.getenv("PATH"), env.get(pathKey));
    }

    public void testApplyEnvironmentPathPrefixOnly()
    {
        Map<String, String> env = new HashMap<String, String>();
        scope.applyEnvironment(env);
        String prefix = scope.getPathPrefix();
        assertEquals(prefix.substring(0, prefix.length() - 1), env.get("PATH"));
    }

    public void testApplyEnvironmentPathOnly()
    {
        PulseScope scope = new PulseScope(); 
        Map<String, String> env = new HashMap<String, String>(System.getenv());
        String pathKey = findPathKey(env);
        scope.applyEnvironment(env);
        assertEquals(System.getenv("PATH"), env.get(pathKey));
    }

    private String findPathKey(Map<String, String> env)
    {
        for(String key: env.keySet())
        {
            if(key.toUpperCase(Locale.US).equals("PATH"))
            {
                return key;
            }
        }

        return null;
    }

    public void testPath()
    {
        List<String> paths = scope.getPathDirectories();
        assertEquals(3, paths.size());
        assertEquals("child resource", paths.get(0));
        assertEquals("child resource", paths.get(1));
        assertEquals("parent resource", paths.get(2));
    }

    public void testPathOrdering()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("first", "first", false, true, false));
        s.add(new ResourceProperty("second", "second", false, true, false));
        
        List<String> paths = s.getPathDirectories();
        assertEquals(2, paths.size());
        assertEquals("second", paths.get(0));
        assertEquals("first", paths.get(1));
    }

    public void testPathOrderingWhenOverriding()
    {
        PulseScope parent = new PulseScope();
        parent.add(new ResourceProperty("first", "first", false, true, false));
        parent.add(new ResourceProperty("second", "second", false, true, false));
        PulseScope child = new PulseScope(parent);
        child.add(new ResourceProperty("first", "override", false, true, false));

        List<String> paths = child.getPathDirectories();
        assertEquals(2, paths.size());
        assertEquals("override", paths.get(0));
        assertEquals("second", paths.get(1));
    }

    public void testGetPathPrefix()
    {
        assertEquals(StringUtils.join(File.pathSeparator, "child resource", "child resource", "parent resource") + File.pathSeparator, scope.getPathPrefix());
    }

    public void testEnvPath()
    {
        Reference reference = scope.getReference("env.PATH");
        assertNotNull(reference);
        assertEquals(scope.getPathPrefix() + System.getenv("PATH"), reference.getValue());
    }

    public void testEnvPathNoPathOrPrefix()
    {
        PulseScope s = new PulseScope();
        assertNull(s.getReference("env.PATH"));
    }

    public void testEnvPathPrefixButNoPath()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("foo", "foo", false, true, false));
        assertEquals("foo", s.getReferenceValue("env.PATH", String.class));
    }

    public void testEnvPathPathButNoPrefix()
    {
        PulseScope s = new PulseScope();
        s.addEnvironmentProperty("PATH", "foo");
        assertEquals("foo", s.getReferenceValue("env.PATH", String.class));
    }

    public void testEnvPathMixedCase()
    {
        PulseScope s = new PulseScope();
        s.addEnvironmentProperty("PatH", "base");
        s.add(new ResourceProperty("foo", "prefix", false, true, false));
        assertEquals("prefix" + File.pathSeparator + "base", s.getReferenceValue("env.PATH", String.class));
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

        Collection<Reference> references = child.getReferences();
        assertEquals(1, references.size());
        Reference reference = references.iterator().next();
        assertEquals("name", reference.getName());
        assertEquals("value", reference.getValue());
    }

    public void testCopy()
    {
        PulseScope original = new PulseScope();
        original.add(new GenericReference<String>("foo", "bar"));

        PulseScope copy = original.copy();
        assertEquals("bar", original.getReferenceValue("foo", String.class));
        assertEquals("bar", copy.getReferenceValue("foo", String.class));
        
        original.add(new GenericReference<String>("foo", "baz"));
        assertEquals("baz", original.getReferenceValue("foo", String.class));
        assertEquals("bar", copy.getReferenceValue("foo", String.class));
    }

    public void testCopyWithParent()
    {
        PulseScope parent = new PulseScope();
        parent.add(new GenericReference<String>("foo", "bar"));
        PulseScope original = new PulseScope(parent);

        PulseScope copy = original.copy();
        assertEquals("bar", parent.getReferenceValue("foo", String.class));
        assertEquals("bar", original.getReferenceValue("foo", String.class));
        assertEquals("bar", copy.getReferenceValue("foo", String.class));

        parent.add(new GenericReference<String>("foo", "baz"));
        assertEquals("baz", parent.getReferenceValue("foo", String.class));
        assertEquals("baz", original.getReferenceValue("foo", String.class));
        assertEquals("bar", copy.getReferenceValue("foo", String.class));
    }

    public void testCopyPreservesLabels()
    {
        PulseScope copy = scope.copy();
        assertEquals("child", copy.getLabel());
        assertEquals("parent", copy.getParent().getLabel());
    }

    public void testCopyTo()
    {
        PulseScope parent = new PulseScope();
        parent.add(new GenericReference<String>("pp", "pv"));
        PulseScope child = new PulseScope(parent);
        child.add(new GenericReference<String>("cp", "cv"));

        PulseScope copy = child.copyTo(parent);
        assertNull(copy.getParent());
        assertEquals(1, copy.getReferences().size());
        assertEquals("cv", copy.getReferenceValue("cp", String.class));
        assertNull(copy.getReference("pp"));
    }

    public void testCopyToNull()
    {
        PulseScope parent = new PulseScope();
        parent.add(new GenericReference<String>("pp", "pv"));
        PulseScope child = new PulseScope(parent);
        child.add(new GenericReference<String>("cp", "cv"));

        assertFullCopy(child.copyTo(null));
    }

    public void testCopyToScopeNotInChain()
    {
        PulseScope parent = new PulseScope();
        parent.add(new GenericReference<String>("pp", "pv"));
        PulseScope child = new PulseScope(parent);
        child.add(new GenericReference<String>("cp", "cv"));

        assertFullCopy(child.copyTo(new PulseScope()));
    }

    public void testCopyToSelf()
    {
        PulseScope parent = new PulseScope();
        parent.add(new GenericReference<String>("pp", "pv"));
        PulseScope child = new PulseScope(parent);
        child.add(new GenericReference<String>("cp", "cv"));

        assertFullCopy(child.copyTo(child));
    }

    public void testCopyToPreservesLabels()
    {
        PulseScope copy = scope.copyTo(null);
        assertEquals("child", copy.getLabel());
        assertEquals("parent", copy.getParent().getLabel());
    }

    private void assertFullCopy(PulseScope copy)
    {
        assertNotNull(copy.getParent());
        assertEquals(2, copy.getReferences().size());
        assertEquals("cv", copy.getReferenceValue("cp", String.class));
        assertEquals("pv", copy.getReferenceValue("pp", String.class));
        assertEquals("pv", copy.getParent().getReferenceValue("pp", String.class));
    }

    public void testGetAncestor()
    {
        assertSame(scope.getParent(), scope.getAncestor("parent"));
    }

    public void testGetAncestorSelf()
    {
        assertSame(scope, scope.getAncestor("child"));
    }

    public void testGetAncestorNonExistant()
    {
        assertNull(scope.getAncestor("foo"));
    }

    public void testGetAncestorSomeNotLabelled()
    {
        PulseScope child = new PulseScope(scope);
        assertSame(scope, child.getAncestor("child"));
    }
    
    public void testGetAncestorGrandparent()
    {
        PulseScope child = new PulseScope(scope);
        assertSame(scope.getParent(), child.getAncestor("parent"));
    }

    // CIB-1976
    public void testOverridingOfEnvironmentVariables()
    {
        String name = "NUMBER_OF_PROCESSORS";

        PulseScope scope = new PulseScope();
        scope.addEnvironmentProperty(name, "1");

        assertEquals("1", scope.getReferenceValue("env." + name, String.class));

        ResourceProperty prop = new ResourceProperty(name, "3", true, false, false);
        scope.add(prop);

        assertEquals("3", scope.getReferenceValue("env." + name, String.class));
    }

    private String getValue(String name)
    {
        Reference reference = scope.getReference(name);
        return (String) reference.getValue();
    }
}
