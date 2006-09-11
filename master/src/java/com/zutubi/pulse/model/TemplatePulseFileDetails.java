package com.zutubi.pulse.model;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.bootstrap.ComponentContext;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.StringWriter;
import java.util.*;

/**
 */
public abstract class TemplatePulseFileDetails extends PulseFileDetails
{
    private List<Capture> captures = new LinkedList<Capture>();
    private List<String> outputProcessors = new LinkedList<String>();
    private VelocityEngine velocityEngine;

    public boolean isBuiltIn()
    {
        return true;
    }

    public String getPulseFile(long id, Project project, Revision revision)
    {
        try
        {
            VelocityContext context = new VelocityContext();
            populateContext(context);
            addPostProcessors(context);
            context.put("details", this);
            context.put("outputProcessors", outputProcessors);
            context.put("captures", captures);

            StringWriter stringWriter = new StringWriter(1024);
            getVelocityEngine().mergeTemplate("pulse-file" + File.separatorChar + getTemplateName(), context, stringWriter);
            return stringWriter.getBuffer().toString();
        }
        catch (Exception e)
        {
            throw new BuildException("Loading template pulse file: " + e.getMessage(), e);
        }
    }

    private void addPostProcessors(VelocityContext context)
    {
        Set<String> includedProcessors = new TreeSet<String>();
        List<String> templates = new LinkedList<String>();

        for(String processor: outputProcessors)
        {
            addProcessor(includedProcessors, processor, templates);
        }

        for(Capture capture: captures)
        {
            for(String processor: capture.getProcessors())
            {
                addProcessor(includedProcessors, processor, templates);
            }
        }

        context.put("postProcessorTemplates", templates);
    }

    private void addProcessor(Set<String> includedProcessors, String processor, List<String> templates)
    {
        if(!includedProcessors.contains(processor))
        {
            includedProcessors.add(processor);
            templates.add(FileSystemUtils.composeFilename("pulse-file", "post-processors", processor + ".vm"));
        }
    }

    public List<Capture> getCaptures()
    {
        return captures;
    }

    // Only for hibernate
    private void setCaptures(List<Capture> captures)
    {
        this.captures = captures;
    }

    public Capture getCapture(String name)
    {
        for(Capture capture: captures)
        {
            if(capture.getName().equals(name))
            {
                return capture;
            }
        }

        return null;
    }

    public Capture getCapture(long id)
    {
        for(Capture c: captures)
        {
            if(c.getId() == id)
            {
                return c;
            }
        }

        return null;
    }

    public void addCapture(Capture capture)
    {
        captures.add(capture);
    }

    public void removeCapture(Capture capture)
    {
        captures.remove(capture);
    }

    public boolean removeCapture(String name)
    {
        Capture deadMan = null;

        for(Capture c: captures)
        {
            if(c.getName().equals(name))
            {
                deadMan = c;
            }
        }

        if(deadMan != null)
        {
            captures.remove(deadMan);
            return true;
        }

        return false;
    }

    public List<String> getOutputProcessors()
    {
        return outputProcessors;
    }

    // Only for hibernate
    private void setOutputProcessors(List<String> outputProcessors)
    {
        this.outputProcessors = outputProcessors;
    }

    public VelocityEngine getVelocityEngine()
    {
        if(velocityEngine == null)
        {
            velocityEngine = (VelocityEngine) ComponentContext.getBean("velocityEngine");
        }
        return velocityEngine;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine)
    {
        this.velocityEngine = velocityEngine;
    }

    public String getReference(String name)
    {
        // Help out velocity, which appears to be completely retarded when it
        // comes to escaping.
        return "${" + name + "}";
    }

    protected abstract String getTemplateName();

    protected abstract void populateContext(VelocityContext context);

}
