package com.cinnamonbob.core.config;

import com.cinnamonbob.core.Feature;

import java.io.InputStream;
import java.util.List;

/**
 * 
 *
 */
public interface Artifact
{
    void addFeature(Feature feature);
    List<Feature> getFeatures();    
    InputStream getContent();
    String getContentName();
}
