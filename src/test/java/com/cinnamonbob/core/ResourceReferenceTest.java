package com.cinnamonbob.core;

import com.cinnamonbob.ObjectFactory;
import com.cinnamonbob.test.BobTestCase;

/**
 * <class-comment/>
 */
public class ResourceReferenceTest extends BobTestCase
{
    private ResourceReference ref = null;
    private ResourceRepository repo = null;

    public void setUp() throws Exception
    {
        FileLoader loader = new FileLoader();
        loader.setObjectFactory(new ObjectFactory());
        repo = new ResourceRepository();
        repo.setFileLoader(loader);
        ref = new ResourceReference();
        ref.setResourceRepository(repo);
    }

    public void tearDown() throws Exception
    {

    }

    public void testResourceReference() throws Exception
    {
        Scope scope = new Scope();
        repo.load(getInput("testResourceReference"));

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

