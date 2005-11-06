package com.cinnamonbob.core.renderer;

import com.cinnamonbob.core.model.BuildResult;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.File;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * 
 * @author jsankey
 */
public class VelocityBuildResultRenderer implements BuildResultRenderer
{
    private static final Logger LOG = Logger.getLogger(VelocityBuildResultRenderer.class.getName());

    private VelocityEngine velocityEngine;

    public void render(String hostUrl, String project, long projectId, BuildResult result, String type, Writer writer)
    {
        VelocityContext context = new VelocityContext();

        context.put("renderer", this);
        context.put("type", type);
        context.put("hostname", hostUrl);
        context.put("project", project);
        context.put("projectId", projectId);
        context.put("result", result);
        context.put("model", result);

        try
        {
            velocityEngine.mergeTemplate(type + File.separatorChar + "BuildResult.vm", "utf-8", context, writer);
        }
        catch(ResourceNotFoundException e)
        {
            LOG.log(Level.SEVERE, "Could not load template for type '" + type + "'", e);
        }
        catch(Exception e)
        {
            LOG.log(Level.SEVERE, "Could not apply template for type '" + type + "'", e);
        }
    }

    public void setVelocityEngine(VelocityEngine velocityEngine)
    {
        this.velocityEngine = velocityEngine;
    }
}
