package com.cinnamonbob.core;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Artifact
{
    public static final String TYPE_PLAIN = "plain";
    
    private String name;
    private String title;
    private String type;
    private File file;
    /**
     * Maps from category to features of that category.
     */
    private Map<String, List<Feature>> features;
    
    
    public Artifact(ArtifactSpec spec, File file)
    {
        name      = spec.getName();
        title     = spec.getTitle();
        type      = spec.getType();
        this.file = file;
        features  = new TreeMap<String, List<Feature>>();
    }


    public String getName()
    {
        return name;
    }
    
    
    public String getTitle()
    {
        return title;
    }
    
    
    public File getFile()
    {
        return file;
    }
    
    
    public void addFeature(Feature feature)
    {
        String category = feature.getCategory();
        
        if(features.containsKey(category))
        {
            features.get(category).add(feature);
        }
        else
        {
            List<Feature> list = new LinkedList<Feature>();
            list.add(feature);
            features.put(category, list);
        }
    }
    
    
    public boolean hasFeatures()
    {
        return features.size() != 0;
    }
    
    
    public Iterator<String> getCategories()
    {
        return features.keySet().iterator();
    }
    
    
    public Iterator<Feature> getFeatures(String category)
    {
        if(features.containsKey(category))
        {
            return features.get(category).iterator();
        }
        else
        {
            return null;
        }
    }
}
