package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.util.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 */
public class XCodePostProcessorTest extends PostProcessorTestBase
{
    private XCodePostProcessor pp;

    public void setUp() throws IOException
    {
        super.setUp();
        pp = new XCodePostProcessor();
    }

    public void tearDown()
    {
        super.tearDown();
        pp = null;
    }

    public void testSimple() throws Exception
    {
        CommandResult result = createAndProcessArtifact(getName(), pp);
        assertTrue(result.failed());
        List<Feature> features = artifact.getFeatures();
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
        pp.setLeadingContext(0);
        pp.setTrailingContext(0);

        CommandResult result = createAndProcessArtifact("testSimple", pp);
        assertTrue(result.failed());
        List<Feature> features = artifact.getFeatures();
        assertEquals(3, features.size());
        assertEquals("some warning: here", features.get(0).getSummary());
        assertEquals("Uncaught exception: Blah", features.get(1).getSummary());
        assertEquals("There was an Assertion failure", features.get(2).getSummary());
    }

    public void testPerformance() throws Exception
    {
        File f = new File(tempDir, "f");
        FileWriter writer = new FileWriter(f);
        for(int i = 0; i < 10000; i++)
        {
            for (int j = 0; j < 10; j++)
            {
                writer.write("0123456789");
            }
            writer.write('\n');
        }
        writer.close();
        artifact = new StoredFileArtifact(f.getName());

        long startTime = System.currentTimeMillis();
        CommandResult result = processArtifact(pp);
        long duration = System.currentTimeMillis() - startTime;
        System.out.printf("Processing took %.02f seconds\n", duration / 1000.0);
        assertTrue(duration < Constants.SECOND * 20);
        assertTrue(result.succeeded());
        assertEquals(0, artifact.getFeatures().size());
    }
}
