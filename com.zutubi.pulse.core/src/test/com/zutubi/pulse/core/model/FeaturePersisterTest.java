package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.api.XMLUtils;
import com.zutubi.util.FileSystemUtils;
import nu.xom.ParsingException;

import java.io.File;
import java.io.IOException;

/**
 */
public class FeaturePersisterTest extends PulseTestCase
{
    private File tempDir;
    private FeaturePersister persister;

    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir(FeaturePersisterTest.class.getName(), "");
        persister = new FeaturePersister();
    }

    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tempDir);
    }

    public void testNoArtifacts() throws Exception
    {
        CommandResult result = new CommandResult("dummy");
        roundTrip(result);
    }

    public void testSingleFeature() throws Exception
    {
        CommandResult result = getResultWithFeatures(new PersistentPlainFeature(Feature.Level.ERROR, "summary here", 10));
        roundTrip(result);
    }

    public void testFeatureLines() throws Exception
    {
        CommandResult result = getResultWithFeatures(getFeature("summary here"));
        roundTrip(result);
    }

    public void testComplexSummary() throws Exception
    {
        CommandResult result = getResultWithFeatures(new PersistentPlainFeature(Feature.Level.WARNING, "this is a summary that\nhas multiple lines & special\n<characters> in /> it <", 1, 10, 3));
        roundTrip(result);
    }

    public void testControlCharacterINSummary() throws Exception
    {
        CommandResult result = getResultWithFeatures(new PersistentPlainFeature(Feature.Level.ERROR, "summary\u0000here", 10));
        roundTrip(result);
    }

    public void testArtifactNoFeatures() throws Exception
    {
        CommandResult result = getResultWithFeatures();
        roundTrip(result);
    }

    public void testMultipleFeatures() throws Exception
    {
        CommandResult result = getResultWithFeatures(getFeature("summary here"), getFeature("another summary"), getFeature("yet another"));
        roundTrip(result);
    }

    public void testSomeArtifactsWithFeatures() throws Exception
    {
        CommandResult result = getResultWithFeatures(getFeature("summary here"));
        result.addArtifact(new StoredArtifact("featureless artifact"));
        roundTrip(result);
    }

    public void testFeatureWhitespacePreserved() throws IOException, ParsingException
    {
        CommandResult result = getResultWithFeatures(getFeature("\nleading and trailing whitespace  "));
        roundTrip(result);
    }

    private PersistentPlainFeature getFeature(String summary)
    {
        return new PersistentPlainFeature(Feature.Level.INFO, summary, 1, 10, 3);
    }

    private CommandResult getResultWithFeatures(PersistentPlainFeature... features)
    {
        CommandResult result = new CommandResult("dummy");
        StoredArtifact artifact = new StoredArtifact("artifact");
        StoredFileArtifact file = new StoredFileArtifact("path/to/file");
        result.addArtifact(artifact);
        artifact.add(file);
        for(PersistentPlainFeature feature: features)
        {
            file.addFeature(feature);
        }
        return result;
    }

    private void roundTrip(CommandResult result) throws IOException, ParsingException
    {
        result.setOutputDir(".");
        persister.writeFeatures(result, tempDir);
        String expected = getFeatureDescription(result);
        nukeFeatures(result);
        persister.readFeatures(result, tempDir);
        assertEquals(expected, getFeatureDescription(result));
    }

    private String getFeatureDescription(CommandResult result)
    {
        StringBuffer description = new StringBuffer();
        for(StoredArtifact a: result.getArtifacts())
        {
            description.append(a.getName()).append('\n');
            for(StoredFileArtifact fa: a.getChildren())
            {
                description.append("  ").append(fa.getPath()).append('\n');
                for(PersistentFeature f: fa.getFeatures())
                {
                    PersistentPlainFeature pf = (PersistentPlainFeature) f;
                    description.append("    ").append(pf.getLevel()).append(':').append(pf.getFirstLine()).append(':').append(pf.getLastLine()).append(':').append(pf.getLineNumber()).append(':').append(XMLUtils.removeIllegalCharacters(pf.getSummary())).append('\n');
                }
            }
        }

        return description.toString();
    }

    private void nukeFeatures(CommandResult result)
    {
        for(StoredArtifact a: result.getArtifacts())
        {
            for(StoredFileArtifact fa: a.getChildren())
            {
                fa.getFeatures().clear();
            }
        }
    }
}
