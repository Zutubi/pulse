package com.cinnamonbob.core;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.File;
import java.io.Writer;

/**
 * 
 * 
 * @author jsankey
 */
public class VelocityBuildResultRenderer implements BuildResultRenderer
{
    private static final String TEMPLATE_DIR = "templates";    
    
    private Bob theBuilder;
    
    
    public VelocityBuildResultRenderer(Bob theBuilder)
    {
        this.theBuilder = theBuilder;
        
        try
        {
            File templateDir = new File(theBuilder.getRootDir(), TEMPLATE_DIR);
            
            Velocity.setProperty("file.resource.loader.path", templateDir.getAbsolutePath());
            Velocity.init();
        }
        catch(Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    public void render(BuildResult result, String type, Writer writer)
    {
        VelocityContext context = new VelocityContext();

        context.put("renderer", this);
        context.put("type", type);
        context.put("result", result);

        try
        {
            Velocity.mergeTemplate(type + "/BuildResult.vm", "utf-8", context, writer);
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
