package com.zutubi.pulse.core;

/**
 */
public class MakePostProcessorLoadTest extends FileLoaderTestBase
{
    private MakePostProcessor helper(String ppName) throws PulseException
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("basic"), bf);

        Scope globalScope = bf.getScope();
        assertTrue(globalScope.containsReference(ppName));
        assertTrue(globalScope.getReference(ppName) instanceof MakePostProcessor);

        return (MakePostProcessor) globalScope.getReference(ppName);
    }

    public void testBasic() throws PulseException
    {
        MakePostProcessor pp = helper("basic");
        assertEquals(true, pp.getFailOnError());
        assertEquals(4, pp.getLeadingContext());
        assertEquals(5, pp.getTrailingContext());
    }
}
