package com.cinnamonbob.core.model;

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

    public StoredFileArtifact()
    {

    }

    public StoredFileArtifact(String path)
    {
        this(path, null);
    }

    public StoredFileArtifact(String path, String type)
    {
        this.path = path;
        this.type = type;
        features = new LinkedList<Feature>();
    }

    public String getPath()
    {
        return path;
    }

    private void setPath(String path)
    {
        this.path = path;
    }

    public void addFeature(Feature feature)
    {
        features.add(feature);
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

}
