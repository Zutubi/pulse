package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.PulseExecutionContext;
import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_OUTPUT_DIR;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.PersistentFeature;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.postprocessors.api.PostProcessor;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorContext;
import com.zutubi.pulse.core.test.api.PulseTestCase;
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

    public void tearDown() throws Exception
    {
        removeDirectory(tempDir);
        super.tearDown();
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

        PostProcessorContext ppContext = createContext(commandResult, context);
        pp.process(new File(tempDir, artifact.getPath()), ppContext);
        commandResult.complete();
        return commandResult;
    }

    private PostProcessorContext createContext(CommandResult commandResult, ExecutionContext context)
    {
        return new DefaultPostProcessorContext(artifact, commandResult, context);
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
        List<PersistentFeature> features = artifact.getFeatures(level);
        assertEquals(summaries.length, features.size());
        for(int i = 0; i < summaries.length; i++)
        {
            assertEquals(summaries[i], features.get(i).getSummary());
        }
    }
}
