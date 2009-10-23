package com.zutubi.pulse.core.commands.xcode;

import com.zutubi.pulse.core.commands.core.RegexPostProcessor;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.PersistentFeature;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.postprocessors.PostProcessorTestBase;
import com.zutubi.util.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 */
public class XCodePostProcessorTest extends PostProcessorTestBase
{
    private RegexPostProcessor pp;

    public void setUp() throws IOException
    {
        super.setUp();
        pp = new RegexPostProcessor(new XCodePostProcessorConfiguration());
    }

    public void testSimple() throws Exception
    {
        CommandResult result = createAndProcessArtifact(getName(), pp);
        assertTrue(result.failed());
        List<PersistentFeature> features = artifact.getFeatures();
        assertEquals(3, features.size());
        assertEquals("buffer line\n" +
                     "some warning: here\n" +
                     "buffer line\n" +
                     "buffer line\n" +
                     "buffer line", features.get(0).getSummary());
        assertEquals("buffer line\n" +
                     "Uncaught exception: Blah\n" +
                     "buffer line\n" +
                     "buffer line\n" +
                     "buffer line", features.get(1).getSummary());
        assertEquals("buffer line\n" +
                     "There was an Assertion failure\n" +
                     "buffer line\n" +
                     "buffer line\n" +
                     "buffer line", features.get(2).getSummary());
    }

    public void testSetContext() throws Exception
    {
        pp.getConfig().setLeadingContext(0);
        pp.getConfig().setTrailingContext(0);

        CommandResult result = createAndProcessArtifact("testSimple", pp);
        assertTrue(result.failed());
        List<PersistentFeature> features = artifact.getFeatures();
        assertEquals(3, features.size());
        assertEquals("some warning: here", features.get(0).getSummary());
        assertEquals("Uncaught exception: Blah", features.get(1).getSummary());
        assertEquals("There was an Assertion failure", features.get(2).getSummary());
    }
}
