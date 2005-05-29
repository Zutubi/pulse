package com.cinnamonbob.core;

/**
 * Contains the common part of a post processor.
 * 
 * @author jsankey
 */
public class PostProcessorCommon
{
    private String name;
    private PostProcessor postProcessor;
        
    public PostProcessorCommon() 
    {
    }    
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setPostProcessor(PostProcessor p)
    {
        this.postProcessor = p;
    }
    
    public void process(Artifact artifact)
    {
        postProcessor.process(artifact);
    }


    public PostProcessor getProcessor()
    {
        return postProcessor;
    }
}
