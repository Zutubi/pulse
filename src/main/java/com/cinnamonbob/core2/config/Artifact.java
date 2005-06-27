package com.cinnamonbob.core2.config;

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
