package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.Reference;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.bean.WiringObjectFactory;

/**
 * Helper base class for file loader tests.
 */
public abstract class FileLoaderTestBase extends PulseTestCase
{
    protected PulseFileLoader loader;
    private WiringObjectFactory objectFactory;

    public void setUp() throws Exception
    {
        super.setUp();

        objectFactory = new WiringObjectFactory();
        objectFactory.initProperties(this);
        
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
        loader.load(getInput(name, "xml"), bf, new ImportingNotSupportedFileResolver());
        return bf;
    }

    protected <T extends Reference> T referenceHelper(String name) throws PulseException
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("basic", "xml"), bf, new ImportingNotSupportedFileResolver());

        Reference r = bf.getReference(name);
        assertNotNull(r);
        return (T) r;
    }

    protected <T extends Command> T commandHelper(String name) throws PulseException
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("basic", "xml"), bf, new ImportingNotSupportedFileResolver());

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
