package com.cinnamonbob.core;

import nu.xom.Element;


/**
 * @author jsankey
 */
public @Deprecated class PostProcessorFactory extends GenericFactory<PostProcessor>
{
    public PostProcessorFactory()
    {
        super(PostProcessor.class, PostProcessorCommon.class, Project.class);
    }
    
    public PostProcessor createPostProcessor(String name, ConfigContext context, Element element, PostProcessorCommon common, Project project) throws ConfigException
    {
        return (PostProcessor)super.create(name, context, element, common, project);
    }
}
