package com.cinnamonbob.model;

import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.model.Revision;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.StringWriter;

/**
 */
public abstract class TemplateBobFileDetails extends BobFileDetails
{
    private VelocityEngine velocityEngine;

    public String getBobFile(Project project, Revision revision)
    {
        try
        {
            VelocityContext context = new VelocityContext();
            populateContext(context);
            StringWriter stringWriter = new StringWriter(1024);
            velocityEngine.mergeTemplate("bob-file" + File.separatorChar + getTemplateName(), context, stringWriter);
            return stringWriter.getBuffer().toString();
        }
        catch (Exception e)
        {
            throw new BuildException("Loading template bob file: " + e.getMessage(), e);
        }
    }

    public void setVelocityEngine(VelocityEngine velocityEngine)
    {
        this.velocityEngine = velocityEngine;
    }

    protected abstract String getTemplateName();

    protected abstract void populateContext(VelocityContext context);

}
