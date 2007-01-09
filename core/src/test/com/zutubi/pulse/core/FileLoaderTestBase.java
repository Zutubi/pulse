package com.zutubi.pulse.core;

import com.zutubi.pulse.test.PulseTestCase;

/**
 * Helper base class for file loader tests.
 */
public abstract class FileLoaderTestBase extends PulseTestCase
{
    protected PulseFileLoader loader;

    public void setUp() throws Exception
    {
        super.setUp();

        ObjectFactory objectFactory = new ObjectFactory();
        PulseFileLoaderFactory fileLoaderFactory = new PulseFileLoaderFactory();
        fileLoaderFactory.setObjectFactory(objectFactory);

        loader = fileLoaderFactory.createLoader();
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
