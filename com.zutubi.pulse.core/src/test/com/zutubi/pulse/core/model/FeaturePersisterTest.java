package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.api.XMLUtils;
import com.zutubi.util.FileSystemUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

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
        removeDirectory(tempDir);

        super.tearDown();
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

    public void testControlCharacterInSummary() throws Exception
    {
        CommandResult result = getResultWithFeatures(new PersistentPlainFeature(Feature.Level.ERROR, "summary\u0000here", 10));
        roundTrip(result);
    }

    public void testNonPlainFeature() throws Exception
    {
        CommandResult result = getResultWithFeatures(new PersistentFeature(Feature.Level.WARNING, "summary"));
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
        result.addArtifact(new StoredArtifact("featureless artifact", true, false));
        roundTrip(result);
    }

    public void testMultipleArtifactsFilesAndFeatures() throws Exception
    {
        CommandResult result = new CommandResult("dummy");

        StoredArtifact a1 = new StoredArtifact("a1", true, false);
        result.addArtifact(a1);
        addFileWithFeatures(a1, getFeature("a1 f1 s1"), getFeature("a1 f1 s2"));
        addFileWithFeatures(a1, getFeature("a1 f2 s1"), getFeature("a1 f2 s2"), getFeature("a1 f2 s3"));
        addFileWithFeatures(a1, getFeature("a1 f3 s1"));

        StoredArtifact a2 = new StoredArtifact("a2", true, false);
        result.addArtifact(a2);
        addFileWithFeatures(a2, getFeature("a2 f1 s1"));
        addFileWithFeatures(a2, getFeature("a2 f2 s1"), getFeature("a2 f2 s2"));
        addFileWithFeatures(a2, getFeature("a2 f3 s1"), getFeature("a2 f3 s2"), getFeature("a2 f3 s3"));
        addFileWithFeatures(a2);

        StoredArtifact a3 = new StoredArtifact("a3", true, false);
        result.addArtifact(a3);
        addFileWithFeatures(a3);
        addFileWithFeatures(a3, getFeature("a3 f2 s1"), getFeature("a3 f2 s2"));
        
        roundTrip(result);
    }

    public void testPerFileArtifactLimit() throws Exception
    {
        CommandResult originalResult = new CommandResult("dummy");

        StoredArtifact a1 = new StoredArtifact("a1", true, false);
        originalResult.addArtifact(a1);
        addFileWithFeatures(a1, getFeature("a1 f1 s1"), getFeature("a1 f1 s2"));
        addFileWithFeatures(a1, getFeature("a1 f2 s1"), getFeature("a1 f2 s2"), getFeature("a1 f2 s3"));
        addFileWithFeatures(a1, getFeature("a1 f3 s1"));

        StoredArtifact a2 = new StoredArtifact("a2", true, false);
        originalResult.addArtifact(a2);
        addFileWithFeatures(a2, getFeature("a2 f1 s1"));
        addFileWithFeatures(a2, getFeature("a2 f2 s1"), getFeature("a2 f2 s2"));
        addFileWithFeatures(a2);
        addFileWithFeatures(a2, getFeature("a2 f4 s1"), getFeature("a2 f4 s2"), getFeature("a2 f4 s3"));

        CommandResult limitedResult = new CommandResult("dummy");

        a1 = new StoredArtifact("a1", true, false);
        limitedResult.addArtifact(a1);
        addFileWithFeatures(a1, getFeature("a1 f1 s1"));
        addFileWithFeatures(a1, getFeature("a1 f2 s1"));
        addFileWithFeatures(a1, getFeature("a1 f3 s1"));

        a2 = new StoredArtifact("a2", true, false);
        limitedResult.addArtifact(a2);
        addFileWithFeatures(a2, getFeature("a2 f1 s1"));
        addFileWithFeatures(a2, getFeature("a2 f2 s1"));
        addFileWithFeatures(a2);
        addFileWithFeatures(a2, getFeature("a2 f4 s1"));
        
        storeLoadAndCompare(originalResult, limitedResult, 1, Integer.MAX_VALUE);
    }
    
    public void testTotalLimit() throws Exception
    {
        CommandResult originalResult = new CommandResult("dummy");

        StoredArtifact a1 = new StoredArtifact("a1", true, false);
        originalResult.addArtifact(a1);
        addFileWithFeatures(a1, getFeature("a1 f1 s1"), getFeature("a1 f1 s2"));
        addFileWithFeatures(a1, getFeature("a1 f2 s1"), getFeature("a1 f2 s2"), getFeature("a1 f2 s3"));
        addFileWithFeatures(a1, getFeature("a1 f3 s1"));

        StoredArtifact a2 = new StoredArtifact("a2", true, false);
        originalResult.addArtifact(a2);
        addFileWithFeatures(a2, getFeature("a2 f1 s1"));
        addFileWithFeatures(a2, getFeature("a2 f2 s1"), getFeature("a2 f2 s2"));
        addFileWithFeatures(a2);
        addFileWithFeatures(a2, getFeature("a2 f4 s1"), getFeature("a2 f4 s2"), getFeature("a2 f4 s3"));

        CommandResult limitedResult = new CommandResult("dummy");

        a1 = new StoredArtifact("a1", true, false);
        limitedResult.addArtifact(a1);
        addFileWithFeatures(a1, getFeature("a1 f1 s1"), getFeature("a1 f1 s2"));
        addFileWithFeatures(a1, getFeature("a1 f2 s1"), getFeature("a1 f2 s2"));
        addFileWithFeatures(a1);

        a2 = new StoredArtifact("a2", true, false);
        limitedResult.addArtifact(a2);
        addFileWithFeatures(a2);
        addFileWithFeatures(a2);
        addFileWithFeatures(a2);
        addFileWithFeatures(a2);
        
        storeLoadAndCompare(originalResult, limitedResult, Integer.MAX_VALUE, 4);
    }
    
    public void testBothLimits() throws Exception
    {
        CommandResult originalResult = new CommandResult("dummy");

        StoredArtifact a1 = new StoredArtifact("a1", true, false);
        originalResult.addArtifact(a1);
        addFileWithFeatures(a1, getFeature("a1 f1 s1"), getFeature("a1 f1 s2"));
        addFileWithFeatures(a1, getFeature("a1 f2 s1"), getFeature("a1 f2 s2"), getFeature("a1 f2 s3"));
        addFileWithFeatures(a1, getFeature("a1 f3 s1"));

        StoredArtifact a2 = new StoredArtifact("a2", true, false);
        originalResult.addArtifact(a2);
        addFileWithFeatures(a2, getFeature("a2 f1 s1"), getFeature("a2 f1 s2"));
        addFileWithFeatures(a2, getFeature("a2 f2 s1"), getFeature("a2 f2 s2"));
        addFileWithFeatures(a2, getFeature("a2 f3 s1"), getFeature("a2 f3 s2"));

        CommandResult limitedResult = new CommandResult("dummy");

        a1 = new StoredArtifact("a1", true, false);
        limitedResult.addArtifact(a1);
        addFileWithFeatures(a1, getFeature("a1 f1 s1"), getFeature("a1 f1 s2"));
        addFileWithFeatures(a1, getFeature("a1 f2 s1"), getFeature("a1 f2 s2"));
        addFileWithFeatures(a1, getFeature("a1 f3 s1"));

        a2 = new StoredArtifact("a2", true, false);
        limitedResult.addArtifact(a2);
        addFileWithFeatures(a2, getFeature("a2 f1 s1"), getFeature("a2 f1 s2"));
        addFileWithFeatures(a2, getFeature("a2 f2 s1"));
        addFileWithFeatures(a2);
        
        storeLoadAndCompare(originalResult, limitedResult, 2, 8);
    }
    
    public void testFeatureWhitespacePreserved() throws IOException, XMLStreamException
    {
        CommandResult result = getResultWithFeatures(getFeature("\nleading and trailing whitespace  "));
        roundTrip(result);
    }

    public void testFeaturesDirectoryNotCreatedOnRead() throws IOException, XMLStreamException
    {
        File featuresDir = FeaturePersister.getFeaturesDirectory(tempDir);
        assertFalse(featuresDir.exists());
        persister.readFeatures(new CommandResult("dummy"), tempDir, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertFalse(featuresDir.exists());
    }

    public void testCommandNameEncoding() throws Exception
    {
        PersistentPlainFeature[] features = new PersistentPlainFeature[]{new PersistentPlainFeature(Feature.Level.ERROR, "summary here", 10)};
        CommandResult result = new CommandResult("command !@#$%^&*(){}|[];:',.<>?");
        StoredArtifact artifact = new StoredArtifact("artifact", true, false);
        result.addArtifact(artifact);
        addFileWithFeatures(artifact, features);

        roundTrip(result);
    }

    public void testLoadsNonEncodedCommandNameForCompatbility() throws Exception
    {
        PersistentPlainFeature[] features = new PersistentPlainFeature[]{new PersistentPlainFeature(Feature.Level.ERROR, "summary here", 10)};
        CommandResult result = new CommandResult("command %");
        result.setOutputDir(".");
        StoredArtifact artifact = new StoredArtifact("artifact", true, false);
        result.addArtifact(artifact);
        addFileWithFeatures(artifact, features);

        File file = persister.writeFeatures(result, tempDir);
        assertTrue(file.renameTo(new File(file.getParent(), result.getCommandName() + ".xml")));
        
        String expected = getFeatureDescription(result);
        nukeFeatures(result);
        persister.readFeatures(result, tempDir, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals(expected, getFeatureDescription(result));
    }

    private PersistentPlainFeature getFeature(String summary)
    {
        return new PersistentPlainFeature(Feature.Level.INFO, summary, 1, 10, 3);
    }

    private CommandResult getResultWithFeatures(PersistentFeature... features)
    {
        CommandResult result = new CommandResult("dummy");
        StoredArtifact artifact = new StoredArtifact("artifact", true, false);
        result.addArtifact(artifact);
        addFileWithFeatures(artifact, features);
        return result;
    }

    private void addFileWithFeatures(StoredArtifact artifact, PersistentFeature... features)
    {
        StoredFileArtifact file = new StoredFileArtifact("path/to/file" + (artifact.getChildren().size() + 1));
        artifact.add(file);
        for (PersistentFeature feature: features)
        {
            file.addFeature(feature);
        }
    }

    private void roundTrip(CommandResult result) throws IOException, XMLStreamException
    {
        storeLoadAndCompare(result, result, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    private void storeLoadAndCompare(CommandResult originalResult, CommandResult limitedResult, int perFileArtifactLimit, int totalLimit) throws IOException, XMLStreamException
    {
        originalResult.setOutputDir(".");
        persister.writeFeatures(originalResult, tempDir);
        String expected = getFeatureDescription(limitedResult);
        nukeFeatures(originalResult);
        persister.readFeatures(originalResult, tempDir, perFileArtifactLimit, totalLimit);
        assertEquals(expected, getFeatureDescription(originalResult));
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
                    if (f instanceof PersistentPlainFeature)
                    {
                        PersistentPlainFeature pf = (PersistentPlainFeature) f;
                        description.append("    ").append(pf.getLevel()).append(':').append(pf.getFirstLine()).append(':').append(pf.getLastLine()).append(':').append(pf.getLineNumber()).append(':').append(XMLUtils.removeIllegalCharacters(pf.getSummary())).append('\n');
                    }
                    else
                    {
                        description.append("    ").append(f.getLevel()).append(':').append(XMLUtils.removeIllegalCharacters(f.getSummary())).append('\n');
                    }
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
