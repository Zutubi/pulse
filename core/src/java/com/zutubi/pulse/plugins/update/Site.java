package com.zutubi.pulse.plugins.update;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents an Eclipse-compatible update site containing plugins and
 * features.  Derived from the site.xml file.  Not all Eclipse features may
 * be supported, only those we use for Pulse.
 */
public class Site
{
    private URL url;
    private String description;
    private Map<String, FeatureReference> featureReferences = new TreeMap<String, FeatureReference>();
    private Map<String, Category> categories = new TreeMap<String, Category>();
    private Map<String, URL> archives = new TreeMap<String, URL>();


    public Site(URL url, String description)
    {
        this.url = url;
        this.description = description;
    }

    public URL getURL()
    {
        return url;
    }

    public String getDescription()
    {
        return description;
    }

    public Collection<FeatureReference> getFeatureReferences()
    {
        return featureReferences.values();
    }

    public FeatureReference getFeatureReference(String id)
    {
        return featureReferences.get(id);
    }

    public void addFeatureReference(FeatureReference featureReference)
    {
        featureReferences.put(featureReference.getId(), featureReference);
    }

    public Collection<Category> getCategories()
    {
        return categories.values();
    }

    public Category getCategory(String name)
    {
        return categories.get(name);
    }

    public void addCategory(Category category)
    {
        categories.put(category.getName(), category);
    }

    public URL getArchiveURL(String path)
    {
        return archives.get(path);
    }

    public void addArchive(String path, URL url)
    {
        archives.put(path, url);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("Site:\n");
        sb.append("  url        : ").append(url.toString()).append('\n');
        sb.append("  description: ").append(description).append('\n');
        sb.append("  categories :\n");
        for(Category c: categories.values())
        {
            sb.append(c).append('\n');
        }
        sb.append("  features   :\n");
        for(FeatureReference f: featureReferences.values())
        {
            sb.append(f).append('\n');
        }
        sb.append("  archives   :\n");
        for(Map.Entry<String, URL> a: archives.entrySet())
        {
            sb.append(a.getKey()).append(" -> ").append(a.getValue()).append('\n');
        }
        sb.append("/Site");
        return sb.toString();
    }
}
