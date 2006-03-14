package com.cinnamonbob.core;

import com.cinnamonbob.test.BobTestCase;
import com.cinnamonbob.core.model.Property;

/**
 * Helper base class for file loader tests.
 */
public abstract class FileLoaderTestBase extends BobTestCase
{
    protected FileLoader loader;

    public void setUp() throws Exception
    {
        super.setUp();

        ObjectFactory factory = new ObjectFactory();
        loader = new FileLoader();
        loader.setObjectFactory(factory);

        // initialise the loader some test objects.
        loader.register("reference", SimpleReference.class);
        loader.register("nested", SimpleNestedType.class);
        loader.register("type", SimpleType.class);
        loader.register("some-reference", SomeReference.class);
        loader.register("validateable", SimpleValidateable.class);

        // initialise the loader with some real objects.
        loader.register("property", Property.class);
        loader.register("recipe", Recipe.class);
        loader.register("def", ComponentDefinition.class);
        loader.register("post-processor", PostProcessorGroup.class);
        loader.register("command", CommandGroup.class);
        loader.register("regex", RegexPostProcessor.class);
        loader.register("executable", ExecutableCommand.class);
        loader.register("ant", AntCommand.class);
        loader.register("make", MakeCommand.class);
        loader.register("dependency", Dependency.class);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    //-----------------------------------------------------------------------
    // Generic helpers
    //-----------------------------------------------------------------------

    protected BobFile load(String name) throws BobException
    {
        BobFile bf = new BobFile();
        loader.load(getInput(name), bf);
        return bf;
    }

    protected void errorHelper(String testName, String messageContent)
    {
        try
        {
            load(testName);
            fail();
        }
        catch (BobException e)
        {
            if (!e.getMessage().contains(messageContent))
            {
                fail("Message '" + e.getMessage() + "' does not contain '" + messageContent + "'");
            }
        }
    }

}
