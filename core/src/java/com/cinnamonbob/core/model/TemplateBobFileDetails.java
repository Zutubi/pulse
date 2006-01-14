package com.cinnamonbob.core.model;

import com.cinnamonbob.core.BuildException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;

/**
 */
public abstract class TemplateBobFileDetails extends BobFileDetails
{
    public BobFileSource getSource()
    {
        String data;
        VelocityEngine engine = new VelocityEngine();

        try
        {
            engine.setProperty("file.resource.loader.path", "/home/jsankey/svn/bob/trunk/master/src/templates/bob-file");
            engine.init();
            VelocityContext context = new VelocityContext();
            populateContext(context);
            StringWriter stringWriter = new StringWriter(1024);
            engine.mergeTemplate(getTemplateName(), context, stringWriter);
            data = stringWriter.getBuffer().toString();
        }
        catch (Exception e)
        {
            throw new BuildException("Loading template bob file: " + e.getMessage(), e);
        }

        return new InMemoryBobFileSource(data);
    }

    protected abstract String getTemplateName();

    protected abstract void populateContext(VelocityContext context);
}
