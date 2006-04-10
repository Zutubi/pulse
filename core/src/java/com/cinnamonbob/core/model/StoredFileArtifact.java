package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.util.FileSystemUtils;

import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class StoredFileArtifact extends Entity
{
    public static final String TYPE_PLAIN = "text/plain";

    private String path;
    private String type;
    private List<Feature> features;
    private List<TestResult> tests;

    public StoredFileArtifact()
    {

    }

    public StoredFileArtifact(String path)
    {
        this(path, null);
    }

    public StoredFileArtifact(String path, String type)
    {
        setPath(path);
        this.type = type;
        features = new LinkedList<Feature>();
        tests = new LinkedList<TestResult>();
    }

    public String getPath()
    {
        return path;
    }

    private void setPath(String path)
    {
        // Normalise to the same path separator used in URLs
        this.path = FileSystemUtils.normaliseSeparators(path);
    }

    public void addFeature(Feature feature)
    {
        features.add(feature);
    }

    public void addFeatures(List<Feature> features)
    {
        this.features.addAll(features);
    }

    public boolean hasFeatures()
    {
        return features.size() != 0;
    }

    public Iterable<Feature.Level> getLevels()
    {
        Set<Feature.Level> levels = new TreeSet<Feature.Level>();

        for (Feature f : features)
        {
            levels.add(f.getLevel());
        }

        return levels;
    }

    public boolean hasMessages(Feature.Level level)
    {
        return getFeatures(level).size() > 0;
    }

    public List<Feature> getFeatures(Feature.Level level)
    {
        List<Feature> result = new LinkedList<Feature>();
        for (Feature f : features)
        {
            if (f.getLevel() == level)
            {
                result.add(f);
            }
        }

        return result;
    }

    public String getType()
    {
        return type;
    }

    private void setType(String type)
    {
        this.type = type;
    }

    public List<Feature> getFeatures()
    {
        return features;
    }

    private void setFeatures(List<Feature> features)
    {
        this.features = features;
    }

    public boolean canDecorate()
    {
        String mimeType;

        if (type == null)
        {
            mimeType = URLConnection.guessContentTypeFromName(path);
        }
        else
        {
            mimeType = type;
        }

        if (mimeType != null && mimeType.equals(TYPE_PLAIN))
        {
            return true;
        }

        for (Feature f : features)
        {
            if (f instanceof PlainFeature)
            {
                return true;
            }
        }

        return false;
    }

    public List<TestResult> getTests()
    {
        return tests;
    }

    public void setTests(List<TestResult> tests)
    {
        this.tests = tests;
    }

    public void addTest(TestResult test)
    {
        tests.add(test);
    }

    public boolean hasBrokenTests()
    {
        return getTestSummary().getBroken() > 0;
    }

    public TestResultSummary getTestSummary()
    {
        TestResultSummary summary = new TestResultSummary();
        accumulateTestSummary(summary);
        return summary;
    }

    public void accumulateTestSummary(TestResultSummary summary)
    {
        for (TestResult test : tests)
        {
            test.accumulateSummary(summary);
        }
    }

}
