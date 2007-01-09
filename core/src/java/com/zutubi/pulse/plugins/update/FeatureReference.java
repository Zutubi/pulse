package com.zutubi.pulse.plugins.update;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * A reference to a feature that is available on an update site.
 */
public class FeatureReference
{
    /**
     * The identifier of the feature.
     */
    private String id;
    /**
     * The version of the feature.
     */
    private String version;
    /**
     * The URL where the feature is located.  Typically:
     *     <site root>/features/<feature id>_<feature version>.jar
     */
    private URL url;
    /**
     * The categories that we are within.
     */
    private List<Category> categories = new LinkedList<Category>();

    public FeatureReference(String id, String version, URL url)
    {
        this.id = id;
        this.version = version;
        this.url = url;
    }


    public String getId()
    {
        return id;
    }

    public String getVersion()
    {
        return version;
    }

    public URL getUrl()
    {
        return url;
    }

    public List<Category> getCategories()
    {
        return categories;
    }

    public void addCategory(Category category)
    {
        categories.add(category);
    }


    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append("FeatureReference:\n");
        sb.append("  id        : ").append(id).append('\n');
        sb.append("  version   : ").append(version).append('\n');
        sb.append("  url       : ").append(url).append('\n');
        sb.append("/FeatureReference");

        return sb.toString();
    }
}
