package com.cinnamonbob.core;

import junit.framework.TestCase;

import java.io.InputStream;

import com.cinnamonbob.ObjectFactory;

/**
 * <class-comment/>
 */
public class ResourceReferenceTest extends TestCase
{
    private ResourceReference ref = null;
    private ResourceRepository repo = null;

    public void setUp() throws Exception
    {
        FileLoader loader = new FileLoader();
        loader.setObjectFactory(new ObjectFactory());
        repo = new ResourceRepository();
        repo.setLoader(loader);
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

    private InputStream getInput(String testName)
    {
        return getClass().getResourceAsStream(getClass().getSimpleName() + "."+testName+".xml");
    }
}

