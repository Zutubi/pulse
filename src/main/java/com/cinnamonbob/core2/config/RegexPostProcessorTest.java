package com.cinnamonbob.core2.config;

import junit.framework.TestCase;

import java.io.*;
import java.util.List;
import java.util.LinkedList;

/**
 * 
 *
 */
public class RegexPostProcessorTest extends TestCase
{
    public void testMatchContent()
    {
        RegexPostProcessor pp = new RegexPostProcessor();
        RegexPattern pattern = pp.createPattern();
        pattern.setCategory("Warning");
        pattern.setExpression(".*[Ww]arning.*");
        
        MockArtifact artifact = new MockArtifact("Warning, line one should be returned\n" +
                "Line two should not.\n" +
                "warning line three should...");
        pp.process(artifact);
        
        List<Feature> features = artifact.getFeatures();
        assertEquals(2, features.size());        
    }

    /**
     * 
     */ 
    private class MockArtifact implements Artifact
    {
        private String content;
        private List<Feature> features = new LinkedList<Feature>();
        public MockArtifact(String content)
        {
            this.content = content;
        }
        
        public void addFeature(Feature feature)
        {
            features.add(feature);
        }

        public List<Feature> getFeatures()
        {
            return features;
        }
        
        public InputStream getContent()
        {
            return new ByteArrayInputStream(content.getBytes());
        }

        public String getContentName()
        {
            return "TestArtifactContent";
        }
    }
}
