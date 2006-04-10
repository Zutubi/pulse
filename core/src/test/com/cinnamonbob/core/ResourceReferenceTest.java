package com.zutubi.pulse.core;

import com.zutubi.pulse.test.BobTestCase;

/**
 * <class-comment/>
 */
public class ResourceReferenceTest extends BobTestCase
{
    public void setUp() throws Exception
    {

    }

    public void tearDown() throws Exception
    {

    }

    public void testResourceReference() throws Exception
    {
        Scope scope = new Scope();
        FileResourceRepository repo = ResourceFileLoader.load(getInput("testResourceReference"));
        ResourceReference ref = new ResourceReference();
        ref.setResourceRepository(repo);

        ref.setScope(scope);
        ref.setName("aResource");
        ref.setVersion("aVersion");
        ref.initBeforeChildren();

        assertTrue(scope.containsReference("b"));
        assertTrue(scope.containsReference("d"));
        assertEquals("c", scope.getReference("b").getValue());
        assertEquals("e", scope.getReference("d").getValue());

        assertFalse(scope.containsReference("1"));
        assertFalse(scope.containsReference("3"));
    }
}

