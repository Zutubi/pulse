package com.zutubi.pulse.core;

import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.bean.ObjectFactory;

/**
 * Helper base class for file loader tests.
 */
public abstract class FileLoaderTestBase extends PulseTestCase
{
    protected PulseFileLoader loader;

    public void setUp() throws Exception
    {
        super.setUp();

        ObjectFactory objectFactory = new DefaultObjectFactory();
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

        Reference r = bf.getReference(name);
        assertNotNull(r);
        return (T) r;
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
