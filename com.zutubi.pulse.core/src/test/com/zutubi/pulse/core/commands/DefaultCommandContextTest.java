package com.zutubi.pulse.core.commands;

import com.zutubi.pulse.core.NoopPostProcessorConfiguration;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.postprocessors.api.PostProcessor;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorContext;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorFactory;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class DefaultCommandContextTest extends PulseTestCase
{
    private File tmpDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tmpDir = FileSystemUtils.createTempDir(getName());
    }

    @Override
    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tmpDir);
        super.tearDown();
    }

    public void testPerFileFeatureLimit() throws IOException
    {
        CommandResult commandResult = new CommandResult("dummy");
        DefaultCommandContext context = createContextWithFeatures(commandResult, 2, Integer.MAX_VALUE, "fe1", "fe2", "fe3");
        registerArtifact(context, "a1", "f1", "f2");
        registerArtifact(context, "a2", "f1");
        context.processArtifacts();
        context.addArtifactsToResult();

        StoredArtifact a1 = commandResult.getArtifact("a1");
        StoredFileArtifact a1f1 = a1.findFile("a1/f1");
        assertEquals(2, a1f1.getFeatures().size());
        StoredFileArtifact a1f2 = a1.findFile("a1/f2");
        assertEquals(2, a1f2.getFeatures().size());

        StoredArtifact a2 = commandResult.getArtifact("a2");
        StoredFileArtifact a2f1 = a2.findFile("a2/f1");
        assertEquals(2, a2f1.getFeatures().size());
    }

    public void testTotalFileFeatureLimit() throws IOException
    {
        CommandResult commandResult = new CommandResult("dummy");
        DefaultCommandContext context = createContextWithFeatures(commandResult, Integer.MAX_VALUE, 5, "fe1", "fe2", "fe3");
        registerArtifact(context, "a1", "f1", "f2");
        registerArtifact(context, "a2", "f1");
        context.processArtifacts();
        context.addArtifactsToResult();

        StoredArtifact a1 = commandResult.getArtifact("a1");
        StoredFileArtifact a1f1 = a1.findFile("a1/f1");
        assertEquals(3, a1f1.getFeatures().size());
        StoredFileArtifact a1f2 = a1.findFile("a1/f2");
        assertEquals(2, a1f2.getFeatures().size());

        StoredArtifact a2 = commandResult.getArtifact("a2");
        StoredFileArtifact a2f1 = a2.findFile("a2/f1");
        assertEquals(0, a2f1.getFeatures().size());
    }

    public void testBothFileFeatureLimits() throws IOException
    {
        CommandResult commandResult = new CommandResult("dummy");
        DefaultCommandContext context = createContextWithFeatures(commandResult, 2, 3, "fe1", "fe2", "fe3");
        registerArtifact(context, "a1", "f1", "f2");
        registerArtifact(context, "a2", "f1");
        context.processArtifacts();
        context.addArtifactsToResult();

        StoredArtifact a1 = commandResult.getArtifact("a1");
        StoredFileArtifact a1f1 = a1.findFile("a1/f1");
        assertEquals(2, a1f1.getFeatures().size());
        StoredFileArtifact a1f2 = a1.findFile("a1/f2");
        assertEquals(1, a1f2.getFeatures().size());

        StoredArtifact a2 = commandResult.getArtifact("a2");
        StoredFileArtifact a2f1 = a2.findFile("a2/f1");
        assertEquals(0, a2f1.getFeatures().size());
    }

    private DefaultCommandContext createContextWithFeatures(CommandResult commandResult, int perFileFeatureLimit, int totalFeatureLimit, final String... features)
    {
        PulseExecutionContext executionContext = new PulseExecutionContext();
        executionContext.addValue(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_OUTPUT_DIR, tmpDir.getAbsolutePath());
        return new DefaultCommandContext(executionContext, commandResult, perFileFeatureLimit, totalFeatureLimit, new PostProcessorFactory()
        {
            public PostProcessor create(PostProcessorConfiguration configuration)
            {
                return new PostProcessor()
                {
                    public void process(File artifactFile, PostProcessorContext ppContext)
                    {
                        for (String feature: features)
                        {
                            ppContext.addFeature(new Feature(Feature.Level.ERROR, feature));
                        }
                    }
                };
            }
        });
    }

    private void registerArtifact(DefaultCommandContext context, String name, String... files) throws IOException
    {
        context.registerArtifact(name, "type", true, false, null);
        context.registerProcessors(name, Arrays.<PostProcessorConfiguration>asList(new NoopPostProcessorConfiguration()));
        File artifactDir = new File(tmpDir, name);
        for (String file: files)
        {
            File artifactFile = new File(artifactDir, file);
            assertTrue(artifactFile.createNewFile());
        }
    }
}
