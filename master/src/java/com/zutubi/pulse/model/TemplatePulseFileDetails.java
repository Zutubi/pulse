/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.model.Revision;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.StringWriter;

/**
 */
public abstract class TemplatePulseFileDetails extends PulseFileDetails
{
    private VelocityEngine velocityEngine;

    public String getPulseFile(long id, Project project, Revision revision)
    {
        try
        {
            VelocityContext context = new VelocityContext();
            populateContext(context);
            StringWriter stringWriter = new StringWriter(1024);
            velocityEngine.mergeTemplate("pulse-file" + File.separatorChar + getTemplateName(), context, stringWriter);
            return stringWriter.getBuffer().toString();
        }
        catch (Exception e)
        {
            throw new BuildException("Loading template pulse file: " + e.getMessage(), e);
        }
    }

    public void setVelocityEngine(VelocityEngine velocityEngine)
    {
        this.velocityEngine = velocityEngine;
    }

    protected abstract String getTemplateName();

    protected abstract void populateContext(VelocityContext context);

}
