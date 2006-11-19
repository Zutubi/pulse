package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Tests for RegexPostProcessor.
 */
public abstract class PostProcessorTestBase extends PulseTestCase
{
    protected StoredFileArtifact artifact;
    protected File tempDir;

    public void setUp() throws IOException
    {
        tempDir = FileSystemUtils.createTempDir(getClass().getName(), "");
    }

    public void tearDown()
    {
        FileSystemUtils.rmdir(tempDir);
        artifact = null;
    }

    protected CommandResult createAndProcessArtifact(String name, PostProcessor pp) throws Exception
    {
        createArtifact(name);
        CommandResult commandResult = new CommandResult("test");
        commandResult.commence();
        pp.process(artifact, commandResult, new CommandContext(null, tempDir, null));
        commandResult.complete();
        return commandResult;
    }

    protected void createArtifact(String name) throws Exception
    {
        URL url = getInputURL(name, "txt");
        File fromFile = new File(url.toURI());
        File toFile = new File(tempDir, fromFile.getName());
        IOUtils.copyFile(fromFile, toFile);
        artifact = new StoredFileArtifact(toFile.getName());
    }

    protected void assertErrors(String... summaries)
    {
        assertFeatures(Feature.Level.ERROR, summaries);
    }

    protected void assertWarnings(String... summaries)
    {
        assertFeatures(Feature.Level.WARNING, summaries);
    }

    protected void assertFeatures(Feature.Level level, String... summaries)
    {
        List<Feature> features = artifact.getFeatures(level);
        assertEquals(summaries.length, features.size());
        for(int i = 0; i < summaries.length; i++)
        {
            assertEquals(summaries[i], features.get(i).getSummary());
        }
    }
}
