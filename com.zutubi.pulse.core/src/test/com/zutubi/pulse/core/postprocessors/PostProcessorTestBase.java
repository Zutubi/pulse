package com.zutubi.pulse.core.postprocessors;

import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_OUTPUT_DIR;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Helper base for post-processor tests.
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
        return processArtifact(pp);
    }

    protected void createArtifact(String name) throws Exception
    {
        URL url = getInputURL(name, "txt");
        File fromFile = new File(url.toURI());
        File toFile = new File(tempDir, fromFile.getName());
        IOUtils.copyFile(fromFile, toFile);
        artifact = new StoredFileArtifact(toFile.getName());
    }

    protected CommandResult processArtifact(PostProcessor pp)
    {
        CommandResult commandResult = new CommandResult("test");
        commandResult.commence();

        ExecutionContext context = new PulseExecutionContext();
        context.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, tempDir.getAbsolutePath());

        pp.process(artifact, commandResult, context);
        commandResult.complete();
        return commandResult;
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
