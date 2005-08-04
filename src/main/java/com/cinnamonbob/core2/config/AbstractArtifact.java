package com.cinnamonbob.core2.config;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import com.cinnamonbob.model.Feature;

/**
 * 
 *
 */
public abstract class AbstractArtifact implements Artifact
{
    protected String name;
    
    private List<Feature> features = new LinkedList<Feature>();

    public void addFeature(Feature feature)
    {
        features.add(feature);
    }
    
    public List<Feature> getFeatures()
    {
        return Collections.unmodifiableList(features);
    }

    public String getContentName()
    {
        return name;
    }
}
