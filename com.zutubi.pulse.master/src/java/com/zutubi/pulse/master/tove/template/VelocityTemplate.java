package com.zutubi.pulse.master.tove.template;

import org.apache.velocity.context.Context;

import java.io.Writer;

/**
 * A wrapper around a velocity template implementation.
 */
public class VelocityTemplate implements Template
{
    private org.apache.velocity.Template delegate;

    public VelocityTemplate(org.apache.velocity.Template template)
    {
        this.delegate = template;
    }

    public void process(Object context, Writer writer) throws Exception
    {
        delegate.merge((Context) context, writer);
    }

    public org.apache.velocity.Template getTemplate()
    {
        return delegate;
    }
}
