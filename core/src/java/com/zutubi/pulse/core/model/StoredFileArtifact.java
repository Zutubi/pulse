package com.zutubi.pulse.core.model;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.StringUtils;

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
    private List<Feature> features = new LinkedList<Feature>();

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

    public String getPathUrl()
    {
        String[] pieces = path.split("/");
        StringBuilder result = new StringBuilder(path.length() * 2);
        for(String piece: pieces)
        {
            if(result.length() > 0)
            {
                result.append('/');
            }

            result.append(StringUtils.uriComponentEncode(piece));
        }

        return result.toString();
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
}
