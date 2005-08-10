package com.cinnamonbob.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.cinnamonbob.core.config.FileArtifact;

public class StoredArtifact extends Entity
{
    public static final String TYPE_PLAIN = "text/plain";
    
    private String name;
    private String title;
    private String type;
    private String file;
    private List<Feature> features;
    
    public StoredArtifact()
    {
        
    }
    
    public StoredArtifact(FileArtifact artifact, String file)
    {
        name      = artifact.getName();
        title     = artifact.getTitle();
        type      = artifact.getType();
        this.file = file;
        features  = new LinkedList<Feature>();
    }


    public String getName()
    {
        return name;
    }
    
    
    public String getTitle()
    {
        return title;
    }
    
    public String getFile()
    {
        return file;
    }
    
    public void addFeature(Feature feature)
    {
        features.add(feature);
    }
    
    public boolean hasFeatures()
    {
        return features.size() != 0;
    }
    
    public Iterator<Feature.Level> getLevels()
    {
        Set<Feature.Level> levels = new TreeSet<Feature.Level>();
        
        for(Feature f: features)
        {
            levels.add(f.getLevel());
        }
        
        return levels.iterator();
    }
    
    public List<Feature> getFeatures(Feature.Level level)
    {
        List<Feature> result = new LinkedList<Feature>();
        for(Feature f: features)
        {
            if(f.getLevel() == level)
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

    private List<Feature> getFeatures()
    {   
        return features;
    }

    private void setFeatures(List<Feature> features)
    {
        this.features = features;
    }


    private void setFile(String file)
    {
        this.file = file;
    }


    private void setName(String name)
    {
        this.name = name;
    }


    private void setTitle(String title)
    {
        this.title = title;
    }


    private void setType(String type)
    {
        this.type = type;
    }
}
