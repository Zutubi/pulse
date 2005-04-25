package com.cinnamonbob.core;

import com.cinnamonbob.setup.StartupManager;
import com.cinnamonbob.bootstrap.velocity.VelocityManager;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.File;
import java.io.Writer;
import java.util.logging.Logger;

/**
 * 
 * 
 * @author jsankey
 */
public class VelocityBuildResultRenderer implements BuildResultRenderer
{

    private static final Logger LOG = Logger.getLogger(VelocityBuildResultRenderer.class.getName());

    public VelocityBuildResultRenderer(Bob theBuilder)
    {

    }
    
    public void render(BuildResult result, String type, Writer writer)
    {
        VelocityContext context = new VelocityContext();

        context.put("renderer", this);
        context.put("type", type);
        context.put("result", result);

        try
        {
            VelocityEngine engine = VelocityManager.getEngine();
            engine.mergeTemplate(type + File.separatorChar + "BuildResult.vm", "utf-8", context, writer);
        }
        catch(ResourceNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch(ParseErrorException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch(MethodInvocationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch(Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    
    public String findTemplate(String type, CommandResult result)
    {
        return type + "/" + result.getClass().getSimpleName() + ".vm";
    }
}
