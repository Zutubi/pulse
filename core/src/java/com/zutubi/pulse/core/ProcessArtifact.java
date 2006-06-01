package com.zutubi.pulse.core;

import com.opensymphony.xwork.validator.ValidatorContext;
import com.zutubi.pulse.core.validation.Validateable;

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

    public void validate(ValidatorContext context)
    {
        if(processor == null)
        {
            context.addActionError("Required attribute 'processor' not specified");
        }
    }
}
