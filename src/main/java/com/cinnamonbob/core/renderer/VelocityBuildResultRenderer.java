package com.cinnamonbob.core.renderer;

import com.cinnamonbob.BobServer;
import com.cinnamonbob.bootstrap.velocity.VelocityManager;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.File;
import java.io.Writer;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * 
 * 
 * @author jsankey
 */
public class VelocityBuildResultRenderer implements BuildResultRenderer
{
    private static final Logger LOG = Logger.getLogger(VelocityBuildResultRenderer.class.getName());

    public void render(Project project, BuildResult result, String type, Writer writer)
    {
        VelocityContext context = new VelocityContext();

        context.put("renderer", this);
        context.put("type", type);
        context.put("project", project);
        context.put("result", result);
        context.put("hostname", BobServer.getHostURL());

        try
        {
            VelocityEngine engine = VelocityManager.getEngine();
            engine.mergeTemplate(type + File.separatorChar + "BuildResult.vm", "utf-8", context, writer);
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
}
