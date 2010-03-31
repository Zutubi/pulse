package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.WebUtils;

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
    private String hash;
    private List<PersistentFeature> features = new LinkedList<PersistentFeature>();

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

    /**
     * @return the path of the artifact file, relative to the output
     *         directory for the command
     */
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

            result.append(WebUtils.uriComponentEncode(piece));
        }

        return result.toString();
    }

    public void addFeature(PersistentFeature feature)
    {
        features.add(feature);
    }

    public void addFeatures(List<PersistentFeature> features)
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

        for (PersistentFeature f : features)
        {
            levels.add(f.getLevel());
        }

        return levels;
    }

    public boolean hasMessages(Feature.Level level)
    {
        return getFeatures(level).size() > 0;
    }

    public List<PersistentFeature> getFeatures(Feature.Level level)
    {
        List<PersistentFeature> result = new LinkedList<PersistentFeature>();
        for (PersistentFeature f : features)
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

    public String getHash()
    {
        return hash;
    }

    public void setHash(String hash)
    {
        this.hash = hash;
    }

    public List<PersistentFeature> getFeatures()
    {
        return features;
    }

    private void setFeatures(List<PersistentFeature> features)
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

        for (PersistentFeature f : features)
        {
            if (f instanceof PersistentPlainFeature)
            {
                return true;
            }
        }

        return false;
    }
}
