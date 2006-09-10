package com.zutubi.pulse.core.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 */
public class StoredArtifact extends Entity
{
    private static final String[] INDEX_NAMES = {"index.html", "index.htm", "default.html", "default.htm"};

    private String name;

    /**
     * Files stored as part of this artifact.  A common special case is just
     * a single file.
     */
    List<StoredFileArtifact> children = new LinkedList<StoredFileArtifact>();

    /**
     * If not null, the name of the index file in this artifact.  Used for
     * browsing the artifact as a HTML report.
     */
    private String index;

    public StoredArtifact()
    {
    }

    public StoredArtifact(String name)
    {
        this.name = name;
    }

    public StoredArtifact(String name, StoredFileArtifact file)
    {
        this.name = name;
        this.children.add(file);
    }

    public String getName()
    {
        return name;
    }

    private void setName(String name)
    {
        this.name = name;
    }

    public void add(StoredFileArtifact child)
    {
        children.add(child);
    }

    public List<StoredFileArtifact> getChildren()
    {
        return children;
    }

    private void setChildren(List<StoredFileArtifact> children)
    {
        this.children = children;
    }

    public boolean isSingleFile()
    {
        return children.size() == 1;
    }

    public StoredFileArtifact getFile()
    {
        return children.get(0);
    }

    public boolean hasFeatures()
    {
        for (StoredFileArtifact child : children)
        {
            if (child.hasFeatures())
            {
                return true;
            }
        }

        return false;
    }

    public Iterable<Feature.Level> getLevels()
    {
        Set<Feature.Level> result = new TreeSet<Feature.Level>();
        for (StoredFileArtifact child : children)
        {
            for (Feature.Level level : child.getLevels())
            {
                result.add(level);
            }
        }

        return result;
    }

    public boolean hasMessages(Feature.Level level)
    {
        for (StoredFileArtifact child : children)
        {
            if (child.hasMessages(level))
            {
                return true;
            }
        }

        return false;
    }

    public List<Feature> getFeatures(Feature.Level level)
    {
        List<Feature> result = new LinkedList<Feature>();
        for (StoredFileArtifact child : children)
        {
            result.addAll(child.getFeatures(level));
        }

        return result;
    }

    public String trimmedPath(StoredFileArtifact artifact)
    {
        String path = artifact.getPath();
        if (path.startsWith(name))
        {
            path = path.substring(name.length());
        }

        if (path.startsWith(File.separator))
        {
            path = path.substring(1);
        }

        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }

        if (path.startsWith("\\"))
        {
            path = path.substring(1);
        }

        return path;
    }

    public String getIndex()
    {
        return index;
    }

    public void setIndex(String index)
    {
        this.index = index;
    }

    public StoredFileArtifact findFile(String filePath)
    {
        for (StoredFileArtifact a : children)
        {
            if (a.getPath().equals(filePath))
            {
                return a;
            }
        }

        return null;
    }

    private StoredFileArtifact findFileBase(String file)
    {
        // Forward slash OK: paths are normalised in these artifacts
        return findFile(name + "/" + file);
    }

    public boolean hasIndexFile()
    {
        return findIndexFile() != null;
    }

    public String findIndexFile()
    {
        if (index != null && findFileBase(index) != null)
        {
            return index;
        }

        for (String index : INDEX_NAMES)
        {
            if (findFileBase(index) != null)
            {
                return index;
            }
        }

        return null;
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
        for (StoredFileArtifact file : children)
        {
            file.accumulateTestSummary(summary);
        }
    }

    public void addAllTestResults(List<TestResult> tests)
    {
        for(StoredFileArtifact file: children)
        {
            file.addAllTestResults(tests);
        }
    }
}
