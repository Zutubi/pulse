package com.zutubi.pulse.core;

import com.zutubi.pulse.test.PulseTestCase;

/**
 * Helper base class for file loader tests.
 */
public abstract class FileLoaderTestBase extends PulseTestCase
{
    protected FileLoader loader;

    public void setUp() throws Exception
    {
        super.setUp();

        ObjectFactory factory = new ObjectFactory();
        loader = new PulseFileLoader();
        loader.setObjectFactory(factory);

        // initialise the loader some test objects.
        ComponentRegistry registry = loader.getRegistry();
        registry.register("dependency", Dependency.class);
        registry.register("reference", SimpleReference.class);
        registry.register("nested", SimpleNestedType.class);
        registry.register("type", SimpleType.class);
        registry.register("some-reference", SomeReference.class);
        registry.register("validateable", SimpleValidateable.class);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    //-----------------------------------------------------------------------
    // Generic helpers
    //-----------------------------------------------------------------------

    protected PulseFile load(String name) throws PulseException
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput(name), bf);
        return bf;
    }

    protected <T extends Reference> T referenceHelper(String name) throws PulseException
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("basic"), bf);

        Scope globalScope = bf.getScope();
        assertTrue(globalScope.containsReference(name));
        return (T) globalScope.getReference(name);
    }

    protected <T extends Command> T commandHelper(String name) throws PulseException
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("basic"), bf);

        Recipe recipe = bf.getRecipes().get(0);
        return (T)recipe.getCommand(name);
    }

    protected void errorHelper(String testName, String messageContent)
    {
        try
        {
            load(testName);
            fail();
        }
        catch (PulseException e)
        {
            if (!e.getMessage().contains(messageContent))
            {
                fail("Message '" + e.getMessage() + "' does not contain '" + messageContent + "'");
            }
        }
    }

}
