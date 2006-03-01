package com.cinnamonbob.renderer;

import com.cinnamonbob.core.util.StringUtils;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.util.logging.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.File;
import java.io.Writer;

/**
 * @author jsankey
 */
public class VelocityBuildResultRenderer implements BuildResultRenderer
{
    private static final Logger LOG = Logger.getLogger(VelocityBuildResultRenderer.class);

    private VelocityEngine velocityEngine;

    public void render(String hostUrl, BuildResult result, String type, Writer writer)
    {
        VelocityContext context = new VelocityContext();

        context.put("renderer", this);
        context.put("type", type);
        context.put("hostname", hostUrl);
        context.put("result", result);
        context.put("model", result);

        try
        {
            velocityEngine.mergeTemplate(type + File.separatorChar + "BuildResult.vm", "utf-8", context, writer);
        }
        catch (ResourceNotFoundException e)
        {
            LOG.severe("Could not load template for type '" + type + "'", e);
        }
        catch (Exception e)
        {
            LOG.severe("Could not apply template for type '" + type + "'", e);
        }
    }

    public String trimmedString(String s, int length)
    {
        return StringUtils.trimmedString(s, length);
    }

    public void setVelocityEngine(VelocityEngine velocityEngine)
    {
        this.velocityEngine = velocityEngine;
    }
}
