package com.cinnamonbob.core;

/**
 */
public class MakePostProcessorLoadTest extends FileLoaderTestBase
{
    private MakePostProcessor helper(String ppName) throws BobException
    {
        BobFile bf = new BobFile();
        loader.load(getInput("basic"), bf);

        Scope globalScope = bf.getGlobalScope();
        assertTrue(globalScope.containsReference(ppName));
        assertTrue(globalScope.getReference(ppName) instanceof MakePostProcessor);

        return (MakePostProcessor) globalScope.getReference(ppName);
    }

    public void testBasic() throws BobException
    {
        MakePostProcessor pp = helper("basic");
        assertEquals(true, pp.getFailOnError());
        assertEquals(4, pp.getLeadingContext());
        assertEquals(5, pp.getTrailingContext());
    }
}
