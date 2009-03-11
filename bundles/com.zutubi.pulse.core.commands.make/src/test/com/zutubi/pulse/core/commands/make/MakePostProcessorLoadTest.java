package com.zutubi.pulse.core.commands.make;

import com.zutubi.pulse.core.FileLoaderTestBase;
import com.zutubi.pulse.core.ImportingNotSupportedFileResolver;
import com.zutubi.pulse.core.PulseFile;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.Reference;

/**
 */
public class MakePostProcessorLoadTest extends FileLoaderTestBase
{
    public void setUp() throws Exception
    {
        super.setUp();

        loader.register("make.pp", MakePostProcessor.class);
    }

    private MakePostProcessor helper(String ppName) throws PulseException
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("basic", "xml"), bf, new ImportingNotSupportedFileResolver());

        Reference r = bf.getReference(ppName);
        assertNotNull(r);
        assertTrue(r instanceof MakePostProcessor);
        return (MakePostProcessor) r;
    }

    public void testBasic() throws PulseException
    {
        MakePostProcessor pp = helper("basic");
        assertEquals(true, pp.isFailOnError());
        assertEquals(4, pp.getLeadingContext());
        assertEquals(5, pp.getTrailingContext());
    }
}
