package com.cinnamonbob.core;

public class ProcessSpec
{
    private PostProcessorCommon processor;
    private ArtifactSpec        artifact;
    
    public ProcessSpec()
    {        
    }
    
    public ArtifactSpec getArtifact()
    {
        return artifact;
    }
    

    public PostProcessorCommon getProcessor()
    {
        return processor;
    }

    public void setProcessor(PostProcessorCommon processor)
    {
        this.processor = processor;
    }

    public void setArtifact(ArtifactSpec artifact)
    {
        this.artifact = artifact;
    }
}
