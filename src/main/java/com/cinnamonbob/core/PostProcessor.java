package com.cinnamonbob.core;

public interface PostProcessor
{
    void process(Artifact artifact);
    
    boolean understandsType(String type);
}
