package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Property;
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
        loader = new PulseFileLoader(factory);
        loader.setObjectFactory(factory);

        // initialise the loader some test objects.
        loader.register("dependency", Dependency.class);
        loader.register("reference", SimpleReference.class);
        loader.register("nested", SimpleNestedType.class);
        loader.register("type", SimpleType.class);
        loader.register("some-reference", SomeReference.class);
        loader.register("validateable", SimpleValidateable.class);
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

        Scope globalScope = bf.getGlobalScope();
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
