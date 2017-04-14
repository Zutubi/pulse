/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.test.EqualityAssertions;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.variables.SimpleVariable;
import com.zutubi.tove.variables.api.Variable;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.util.*;

import static com.google.common.collect.Collections2.transform;
import static java.util.Arrays.asList;

public class PulseScopeTest extends PulseTestCase
{
    public static final String KEY_PARENT_ONLY = "parent only";
    public static final String KEY_PARENT_AND_CHILD = "parent and child";
    public static final String KEY_PARENT_ONLY_RESOURCE = "parent only resource";
    public static final String KEY_PARENT_AND_CHILD_RESOURCE = "parent and child resource";
    public static final String KEY_CHILD_ONLY = "child only";
    public static final String KEY_CHILD_ONLY_RESOURCE = "child only resource";
    public static final String VALUE_PARENT = "parent";
    public static final String VALUE_CHILD = "child";
    public static final String VALUE_CHILD_RESOURCE = "child resource";
    public static final String VALUE_PARENT_RESOURCE = "parent resource";
    public static final String LABEL_PARENT = "label-parent";
    public static final String LABEL_CHILD = "label-child";

    private PulseScope scope;

    protected void setUp() throws Exception
    {
        super.setUp();
        PulseScope parent = new PulseScope();
        parent.setLabel(LABEL_PARENT);
        scope = new PulseScope(parent);
        scope.setLabel(LABEL_CHILD);
        parent.add(new SimpleVariable<String>(KEY_PARENT_ONLY, VALUE_PARENT));
        parent.add(new SimpleVariable<String>(KEY_PARENT_AND_CHILD, VALUE_PARENT));
        parent.add(new ResourceProperty(KEY_PARENT_ONLY_RESOURCE, VALUE_PARENT_RESOURCE, true, true));
        parent.add(new ResourceProperty(KEY_PARENT_AND_CHILD_RESOURCE, VALUE_PARENT_RESOURCE, true, true));

        Map<String, String> env = System.getenv();
        for(Map.Entry<String, String> var: env.entrySet())
        {
            parent.addEnvironmentProperty(var.getKey(), var.getValue());
        }

        scope.add(new SimpleVariable<String>(KEY_CHILD_ONLY, VALUE_CHILD));
        scope.add(new SimpleVariable<String>(KEY_PARENT_AND_CHILD, VALUE_CHILD));
        scope.add(new ResourceProperty(KEY_CHILD_ONLY_RESOURCE, VALUE_CHILD_RESOURCE, true, true));
        scope.add(new ResourceProperty(KEY_PARENT_AND_CHILD_RESOURCE, VALUE_CHILD_RESOURCE, true, true));

        scope.add(new ResourceProperty("not added", "not added", false, false));
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
        assertEquals(VALUE_CHILD, getValue(KEY_CHILD_ONLY));
    }

    public void testOverriddenProperty()
    {
        assertEquals(VALUE_CHILD, getValue(KEY_PARENT_AND_CHILD));
    }


    public void testAddUnique()
    {
        try
        {
            scope.addUnique(new SimpleVariable<String>(KEY_CHILD_ONLY, ""));
            fail();
        }
        catch(IllegalArgumentException e)
        {
            assertEquals("'child only' is already defined in this scope.", e.getMessage());
        }
    }

    public void testAddUniqueOverride()
    {
        scope.addUnique(new SimpleVariable<String>(KEY_PARENT_ONLY, "override"));
        assertEquals("override", scope.getVariableValue(KEY_PARENT_ONLY, String.class));
    }

    public void testAddAll()
    {
        scope.addAll(asList(new SimpleVariable<String>("p1", "v1"), new SimpleVariable<String>("p2", "v2")));
        assertEquals("v1", scope.getVariableValue("p1", String.class));
        assertEquals("v2", scope.getVariableValue("p2", String.class));
    }

    public void testAddAllUnique()
    {
        try
        {
            scope.addAllUnique(asList(new SimpleVariable<String>(KEY_CHILD_ONLY, "")));
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("'child only' is already defined in this scope.", e.getMessage());
        }
    }

    public void testGetVariables()
    {
        PulseScope parent = new PulseScope();
        parent.add(new SimpleVariable<String>(VALUE_PARENT, "pv"));
        parent.add(new SimpleVariable<String>("both", "bv"));
        PulseScope child = new PulseScope(parent);
        child.add(new SimpleVariable<String>(VALUE_CHILD, "cv"));
        child.add(new SimpleVariable<String>("both", "bv"));

        Collection<Variable> variables = child.getVariables();
        assertEquals(3, variables.size());
        EqualityAssertions.assertEquals(asList(VALUE_PARENT, VALUE_CHILD, "both"), Lists.newArrayList(transform(variables, new Function<Variable, String>()
        {
            public String apply(Variable variable)
            {
                return variable.getName();
            }
        })));
    }

    public void testGetVariablesOfType()
    {
        PulseScope scope = new PulseScope();
        scope.add(new SimpleVariable<Long>("long", 1L));
        scope.add(new SimpleVariable<String>("string", "s"));

        Collection<Variable> variables = scope.getVariables(String.class);
        assertEquals(1, variables.size());
        assertEquals("string", variables.iterator().next().getName());
    }

    public void testGetVariablesOfTypeOverrideType()
    {
        PulseScope parent = new PulseScope();
        parent.add(new SimpleVariable<Long>("long then string", 1L));
        parent.add(new SimpleVariable<String>("string then long", "s"));

        PulseScope child = new PulseScope(parent);
        child.add(new SimpleVariable<String>("long then string", "s"));
        child.add(new SimpleVariable<Long>("string then long", 1L));
        
        Collection<Variable> variables = child.getVariables(String.class);
        assertEquals(1, variables.size());
        assertEquals("long then string", variables.iterator().next().getName());
    }

    public void testGetVariableValue()
    {
        assertEquals(VALUE_CHILD, scope.getVariableValue(KEY_CHILD_ONLY, String.class));
    }

    public void testGetVariableValueWrongType()
    {
        assertNull(scope.getVariableValue(KEY_CHILD_ONLY, Long.class));
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
        originalEnvironment.put(KEY_CHILD_ONLY_RESOURCE, "overridden");
        scope.applyEnvironment(originalEnvironment);
        assertEnvironment(originalEnvironment);
        assertEquals("bar", originalEnvironment.get("foo"));
    }

    private void assertEnvironment(Map<String, String> environment)
    {
        assertEquals(VALUE_PARENT_RESOURCE, environment.get(KEY_PARENT_ONLY_RESOURCE));
        assertEquals(VALUE_CHILD_RESOURCE, environment.get(KEY_CHILD_ONLY_RESOURCE));
        assertEquals(VALUE_CHILD_RESOURCE, environment.get(KEY_PARENT_AND_CHILD_RESOURCE));
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
        assertEquals(VALUE_CHILD_RESOURCE, paths.get(0));
        assertEquals(VALUE_CHILD_RESOURCE, paths.get(1));
        assertEquals(VALUE_PARENT_RESOURCE, paths.get(2));
    }

    public void testPathOrdering()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("first", "first", false, true));
        s.add(new ResourceProperty("second", "second", false, true));
        
        List<String> paths = s.getPathDirectories();
        assertEquals(2, paths.size());
        assertEquals("second", paths.get(0));
        assertEquals("first", paths.get(1));
    }

    public void testPathOrderingWhenOverriding()
    {
        PulseScope parent = new PulseScope();
        parent.add(new ResourceProperty("first", "first", false, true));
        parent.add(new ResourceProperty("second", "second", false, true));
        PulseScope child = new PulseScope(parent);
        child.add(new ResourceProperty("first", "override", false, true));

        List<String> paths = child.getPathDirectories();
        assertEquals(2, paths.size());
        assertEquals("override", paths.get(0));
        assertEquals("second", paths.get(1));
    }

    public void testGetPathPrefix()
    {
        assertEquals(StringUtils.join(File.pathSeparator, VALUE_CHILD_RESOURCE, VALUE_CHILD_RESOURCE, VALUE_PARENT_RESOURCE) + File.pathSeparator, scope.getPathPrefix());
    }

    public void testEnvPath()
    {
        Variable variable = scope.getVariable("env.PATH");
        assertNotNull(variable);
        assertEquals(scope.getPathPrefix() + System.getenv("PATH"), variable.getValue());
    }

    public void testEnvPathNoPathOrPrefix()
    {
        PulseScope s = new PulseScope();
        assertNull(s.getVariable("env.PATH"));
    }

    public void testEnvPathPrefixButNoPath()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("foo", "foo", false, true));
        assertEquals("foo", s.getVariableValue("env.PATH", String.class));
    }

    public void testEnvPathPathButNoPrefix()
    {
        PulseScope s = new PulseScope();
        s.addEnvironmentProperty("PATH", "foo");
        assertEquals("foo", s.getVariableValue("env.PATH", String.class));
    }

    public void testEnvPathMixedCase()
    {
        PulseScope s = new PulseScope();
        s.addEnvironmentProperty("PatH", "base");
        s.add(new ResourceProperty("foo", "prefix", false, true));
        assertEquals("prefix" + File.pathSeparator + "base", s.getVariableValue("env.PATH", String.class));
    }

    public void testReferToEarlierProperty()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("test", "value", false, false));
        s.add(new ResourceProperty("test2", "$(test)2", false, false));
        assertEquals("value2", s.getVariable("test2").getValue());
    }

    public void testSelfVariable()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("testvar", "$(testvar)", false, false));
        assertEquals("$(testvar)", s.getVariable("testvar").getValue());
    }

    public void testReferToParentProperty()
    {
        PulseScope p = new PulseScope();
        PulseScope c = new PulseScope(p);
        p.add(new ResourceProperty("test", "value", false, false));
        c.add(new ResourceProperty("test2", "$(test)2", false, false));
        assertEquals("value2", c.getVariable("test2").getValue());
    }

    public void testAddToPathPreservesOrder()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("j", "jv", false, true));
        s.add(new ResourceProperty("z", "zv", false, true));
        s.add(new ResourceProperty("a", "av", false, true));
        assertEquals(FileSystemUtils.composeSearchPath("av", "zv", "jv") + File.pathSeparatorChar, s.getPathPrefix());
    }

    public void testChildHidesParentAddToPath()
    {
        PulseScope p = new PulseScope();
        PulseScope c = new PulseScope(p);
        p.add(new ResourceProperty("testvar", VALUE_PARENT, false, true));
        c.add(new ResourceProperty("testvar", VALUE_CHILD, false, false));
        assertEquals(VALUE_PARENT + File.pathSeparator, p.getPathPrefix());
        assertEquals("", c.getPathPrefix());
    }

    public void testChildHidesParentAddToEnv()
    {
        PulseScope p = new PulseScope();
        PulseScope c = new PulseScope(p);
        p.add(new ResourceProperty("priceless", VALUE_PARENT, true, false));
        c.add(new ResourceProperty("priceless", VALUE_CHILD, false, false));
        assertFalse(c.containsVariable("env.PRICELESS"));
    }

    public void testAddToEnvironmentAddsEnvVar()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("testvar", "value", true, false));
        assertEquals("value", s.getVariable("env.testvar").getValue());
    }

    public void testAddToParentEnvAddsEnvVar()
    {
        PulseScope p = new PulseScope();
        PulseScope c = new PulseScope(p);
        p.add(new ResourceProperty("testvar", "value", true, false));
        assertEquals("value", c.getVariable("env.testvar").getValue());
    }

    public void testAddToEnvVariableEnvVar()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("testvar", "value", true, false));
        s.add(new ResourceProperty("testvar2", "$(env.testvar)2", true, false));
        assertEquals("value2", s.getVariable("testvar2").getValue());
    }

    public void testAddToParentEnvVariableEnvVar()
    {
        PulseScope p = new PulseScope();
        PulseScope c = new PulseScope(p);
        p.add(new ResourceProperty("testvar", "value", true, false));
        c.add(new ResourceProperty("testvar2", "$(env.testvar)2", true, false));
        assertEquals("value2", c.getVariable("testvar2").getValue());
    }

    public void testVariableEnvPath()
    {
        PulseScope s = new PulseScope();
        s.addEnvironmentProperty("PATH", "dummypath");
        s.add(new ResourceProperty("somevar", "someval", false, true));
        s.add(new ResourceProperty("refvar", "$(env.PATH)?", false, false));
        assertEquals("someval" + File.pathSeparatorChar + "dummypath?", s.getVariable("refvar").getValue());
    }

    public void testSelfVariableEnvVar()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("refvar", "$(env.refvar)?", true, false));
        assertEquals("$(env.refvar)?", s.getVariable("refvar").getValue());
    }

    public void testSelfVariableEnvPath()
    {
        PulseScope s = new PulseScope();
        s.addEnvironmentProperty("PATH", "dummypath");
        s.add(new ResourceProperty("refvar", "$(env.PATH)?", false, true));
        assertEquals("dummypath?", s.getVariable("refvar").getValue());
    }

    public void testBackslash()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("myvar", "\\", false, true));
        assertEquals("\\", s.getVariable("myvar").getValue());
    }

    public void testBadSyntax()
    {
        PulseScope s = new PulseScope();
        s.add(new ResourceProperty("myvar", "this ${ is invalid", false, true));
        assertEquals("this ${ is invalid", s.getVariable("myvar").getValue());
    }

    public void testGetVariablesIncludesParents()
    {
        PulseScope parent = new PulseScope();
        PulseScope child = new PulseScope(parent);

        parent.add(new ResourceProperty("name", "value"));

        Collection<Variable> variables = child.getVariables();
        assertEquals(1, variables.size());
        Variable variable = variables.iterator().next();
        assertEquals("name", variable.getName());
        assertEquals("value", variable.getValue());
    }

    public void testCopy()
    {
        PulseScope original = new PulseScope();
        original.add(new SimpleVariable<String>("foo", "bar"));

        PulseScope copy = original.copy();
        assertEquals("bar", original.getVariableValue("foo", String.class));
        assertEquals("bar", copy.getVariableValue("foo", String.class));
        
        original.add(new SimpleVariable<String>("foo", "baz"));
        assertEquals("baz", original.getVariableValue("foo", String.class));
        assertEquals("bar", copy.getVariableValue("foo", String.class));
    }

    public void testCopyWithParent()
    {
        PulseScope parent = new PulseScope();
        parent.add(new SimpleVariable<String>("foo", "bar"));
        PulseScope original = new PulseScope(parent);

        PulseScope copy = original.copy();
        assertEquals("bar", parent.getVariableValue("foo", String.class));
        assertEquals("bar", original.getVariableValue("foo", String.class));
        assertEquals("bar", copy.getVariableValue("foo", String.class));

        parent.add(new SimpleVariable<String>("foo", "baz"));
        assertEquals("baz", parent.getVariableValue("foo", String.class));
        assertEquals("baz", original.getVariableValue("foo", String.class));
        assertEquals("bar", copy.getVariableValue("foo", String.class));
    }

    public void testCopyPreservesLabels()
    {
        PulseScope copy = scope.copy();
        assertEquals(LABEL_CHILD, copy.getLabel());
        assertEquals(LABEL_PARENT, copy.getParent().getLabel());
    }

    public void testCopyTo()
    {
        PulseScope parent = new PulseScope();
        parent.add(new SimpleVariable<String>("pp", "pv"));
        PulseScope child = new PulseScope(parent);
        child.add(new SimpleVariable<String>("cp", "cv"));

        PulseScope copy = child.copyTo(parent);
        assertNull(copy.getParent());
        assertEquals(1, copy.getVariables().size());
        assertEquals("cv", copy.getVariableValue("cp", String.class));
        assertNull(copy.getVariable("pp"));
    }

    public void testCopyToNull()
    {
        PulseScope parent = new PulseScope();
        parent.add(new SimpleVariable<String>("pp", "pv"));
        PulseScope child = new PulseScope(parent);
        child.add(new SimpleVariable<String>("cp", "cv"));

        assertFullCopy(child.copyTo(null));
    }

    public void testCopyToScopeNotInChain()
    {
        PulseScope parent = new PulseScope();
        parent.add(new SimpleVariable<String>("pp", "pv"));
        PulseScope child = new PulseScope(parent);
        child.add(new SimpleVariable<String>("cp", "cv"));

        assertFullCopy(child.copyTo(new PulseScope()));
    }

    public void testCopyToSelf()
    {
        PulseScope parent = new PulseScope();
        parent.add(new SimpleVariable<String>("pp", "pv"));
        PulseScope child = new PulseScope(parent);
        child.add(new SimpleVariable<String>("cp", "cv"));

        assertFullCopy(child.copyTo(child));
    }

    public void testCopyToPreservesLabels()
    {
        PulseScope copy = scope.copyTo(null);
        assertEquals(LABEL_CHILD, copy.getLabel());
        assertEquals(LABEL_PARENT, copy.getParent().getLabel());
    }

    private void assertFullCopy(PulseScope copy)
    {
        assertNotNull(copy.getParent());
        assertEquals(2, copy.getVariables().size());
        assertEquals("cv", copy.getVariableValue("cp", String.class));
        assertEquals("pv", copy.getVariableValue("pp", String.class));
        assertEquals("pv", copy.getParent().getVariableValue("pp", String.class));
    }

    public void testGetAncestor()
    {
        assertSame(scope.getParent(), scope.getAncestor(LABEL_PARENT));
    }

    public void testGetAncestorSelf()
    {
        assertSame(scope, scope.getAncestor(LABEL_CHILD));
    }

    public void testGetAncestorNonExistant()
    {
        assertNull(scope.getAncestor("foo"));
    }

    public void testGetAncestorSomeNotLabelled()
    {
        PulseScope child = new PulseScope(scope);
        assertSame(scope, child.getAncestor(LABEL_CHILD));
    }
    
    public void testGetAncestorGrandparent()
    {
        PulseScope child = new PulseScope(scope);
        assertSame(scope.getParent(), child.getAncestor(LABEL_PARENT));
    }

    // CIB-1976
    public void testOverridingOfEnvironmentVariables()
    {
        String name = "NUMBER_OF_PROCESSORS";

        PulseScope scope = new PulseScope();
        scope.addEnvironmentProperty(name, "1");

        assertEquals("1", scope.getVariableValue("env." + name, String.class));

        ResourceProperty prop = new ResourceProperty(name, "3", true, false);
        scope.add(prop);

        assertEquals("3", scope.getVariableValue("env." + name, String.class));
    }
    
    public void testFindVariableNonExistent()
    {
        assertNull(scope.findVariable("nosuchvar"));
    }

    public void testFindVariableParent()
    {
        assertSame(scope.getParent(), scope.findVariable(KEY_PARENT_ONLY));
    }
    
    public void testFindVariableChild()
    {
        assertSame(scope, scope.findVariable(KEY_CHILD_ONLY));
    }
    
    public void testFindVariableParentAndChild()
    {
        assertSame(scope, scope.findVariable(KEY_PARENT_AND_CHILD));
    }
    
    private String getValue(String name)
    {
        Variable variable = scope.getVariable(name);
        return (String) variable.getValue();
    }
}
