package com.zutubi.pulse.core;

import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;


/**
 * A reference to a post-processor.
 */
class ProcessArtifact implements Validateable
{
    private PostProcessor processor;

    public PostProcessor getProcessor()
    {
        return processor;
    }

    public void setProcessor(PostProcessor processor)
    {
        this.processor = processor;
    }

    public void validate(ValidationContext context)
    {
        if(processor == null)
        {
            context.addActionError("Required attribute 'processor' not specified");
        }
    }
}
