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
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Mapping;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class DefaultCommandContextTest extends PulseTestCase
{
    private static final String SUMMARY_FILE_REACHED = "Feature limit reached for this file.  Not all features are recorded.";
    
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
        DefaultCommandContext context = createContextWithErrorFeatures(commandResult, 2, "fe1", "fe2", "fe3");
        registerArtifact(context, "a1", "f1", "f2");
        registerArtifact(context, "a2", "f1");
        context.processArtifacts();
        context.addArtifactsToResult();

        StoredArtifact a1 = commandResult.getArtifact("a1");
        StoredFileArtifact a1f1 = a1.findFile("a1/f1");
        assertEquals(2, a1f1.getFeatures().size());
        assertEquals(SUMMARY_FILE_REACHED, a1f1.getFeatures().get(1).getSummary());
        StoredFileArtifact a1f2 = a1.findFile("a1/f2");
        assertEquals(2, a1f2.getFeatures().size());

        StoredArtifact a2 = commandResult.getArtifact("a2");
        StoredFileArtifact a2f1 = a2.findFile("a2/f1");
        assertEquals(2, a2f1.getFeatures().size());
    }

    public void testPerFileFeatureLimitSimpleEviction() throws IOException
    {
        CommandResult commandResult = new CommandResult("dummy");
        DefaultCommandContext context = createContextWithFeatures(commandResult, 3, error("e1"), warning("w1"), warning("w2"), error("e2"));
        registerArtifact(context, "a", "f");
        context.processArtifacts();
        context.addArtifactsToResult();

        StoredArtifact artifact = commandResult.getArtifact("a");
        StoredFileArtifact file = artifact.findFile("a/f");
        assertEquals(3, file.getFeatures().size());
        assertEquals("e1", file.getFeatures().get(0).getSummary());
        assertEquals("e2", file.getFeatures().get(1).getSummary());
        assertEquals(SUMMARY_FILE_REACHED, file.getFeatures().get(2).getSummary());
    }

    public void testPerFileFeatureLimitEvictionExactlyAtLimit() throws IOException
    {
        CommandResult commandResult = new CommandResult("dummy");
        DefaultCommandContext context = createContextWithFeatures(commandResult, 3, error("e1"), warning("w1"), error("e2"), error("e3"));
        registerArtifact(context, "a", "f");
        context.processArtifacts();
        context.addArtifactsToResult();

        StoredArtifact artifact = commandResult.getArtifact("a");
        StoredFileArtifact file = artifact.findFile("a/f");
        assertEquals(3, file.getFeatures().size());
        assertEquals("e1", file.getFeatures().get(0).getSummary());
        assertEquals("e2", file.getFeatures().get(1).getSummary());
        assertEquals(SUMMARY_FILE_REACHED, file.getFeatures().get(2).getSummary());
    }

    public void testPerFileFeatureLimitWarningsUnderLimit() throws IOException
    {
        CommandResult commandResult = new CommandResult("dummy");
        DefaultCommandContext context = createContextWithFeatures(commandResult, 3, warning("w1"), warning("w2"), error("e1"));
        registerArtifact(context, "a", "f");
        context.processArtifacts();
        context.addArtifactsToResult();

        StoredArtifact artifact = commandResult.getArtifact("a");
        StoredFileArtifact file = artifact.findFile("a/f");
        assertEquals(3, file.getFeatures().size());
        assertEquals("w1", file.getFeatures().get(0).getSummary());
        assertEquals("w2", file.getFeatures().get(1).getSummary());
        assertEquals("e1", file.getFeatures().get(2).getSummary());
    }
    
    public void testPerFileFeatureLimitEvictionAfterWarningsReachLimit() throws IOException
    {
        CommandResult commandResult = new CommandResult("dummy");
        DefaultCommandContext context = createContextWithFeatures(commandResult, 3, warning("w1"), warning("w2"), warning("w3"), error("e1"));
        registerArtifact(context, "a", "f");
        context.processArtifacts();
        context.addArtifactsToResult();

        StoredArtifact artifact = commandResult.getArtifact("a");
        StoredFileArtifact file = artifact.findFile("a/f");
        assertEquals(3, file.getFeatures().size());
        assertEquals("w1", file.getFeatures().get(0).getSummary());
        assertEquals("e1", file.getFeatures().get(1).getSummary());
        assertEquals(SUMMARY_FILE_REACHED, file.getFeatures().get(2).getSummary());
    }
    
    public void testPerFileFeatureLimitEvictionAfterWarningsOverLimit() throws IOException
    {
        CommandResult commandResult = new CommandResult("dummy");
        DefaultCommandContext context = createContextWithFeatures(commandResult, 3, warning("w1"), warning("w2"), warning("w3"), warning("w4"), error("e1"));
        registerArtifact(context, "a", "f");
        context.processArtifacts();
        context.addArtifactsToResult();

        StoredArtifact artifact = commandResult.getArtifact("a");
        StoredFileArtifact file = artifact.findFile("a/f");
        assertEquals(3, file.getFeatures().size());
        assertEquals("w1", file.getFeatures().get(0).getSummary());
        assertEquals("e1", file.getFeatures().get(1).getSummary());
        assertEquals(SUMMARY_FILE_REACHED, file.getFeatures().get(2).getSummary());
    }
    
    private DefaultCommandContext createContextWithErrorFeatures(CommandResult commandResult, int perFileFeatureLimit, final String... errors)
    {
        Feature[] features = CollectionUtils.mapToArray(errors, new Mapping<String, Feature>()
        {
            public Feature map(String s)
            {
                return error(s);
            }
        }, new Feature[errors.length]);
        
        return createContextWithFeatures(commandResult, perFileFeatureLimit, features);
    }

    private DefaultCommandContext createContextWithFeatures(CommandResult commandResult, int perFileFeatureLimit, final Feature... features)
    {
        PulseExecutionContext executionContext = new PulseExecutionContext();
        executionContext.addValue(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_OUTPUT_DIR, tmpDir.getAbsolutePath());
        return new DefaultCommandContext(executionContext, commandResult, perFileFeatureLimit, new PostProcessorFactory()
        {
            public PostProcessor create(PostProcessorConfiguration configuration)
            {
                return new PostProcessor()
                {
                    public void process(File artifactFile, PostProcessorContext ppContext)
                    {
                        for (Feature feature: features)
                        {
                            ppContext.addFeature(feature);
                        }
                    }
                };
            }
        });
    }
    
    private Feature error(String message)
    {
        return new Feature(Feature.Level.ERROR, message);
    }

    private Feature warning(String message)
    {
        return new Feature(Feature.Level.WARNING, message);
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
