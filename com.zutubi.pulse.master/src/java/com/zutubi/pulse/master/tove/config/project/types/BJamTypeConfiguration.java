package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.master.tove.config.project.BrowseScmDirAction;
import com.zutubi.pulse.master.tove.config.project.BrowseScmFileAction;
import com.zutubi.util.TextUtils;
import org.apache.velocity.VelocityContext;

/**
 * Boost Jam project.
 */
@SymbolicName("zutubi.bjamTypeConfig")
@Form(fieldOrder = {"work", "file", "target", "args", "postProcessors"})
public class BJamTypeConfiguration extends TemplateTypeConfiguration
{
    @BrowseScmDirAction
    private String work;
    @BrowseScmFileAction
    private String file;
    private String target;
    private String args;

    public String getWork()
    {
        return work;
    }

    public void setWork(String work)
    {
        this.work = work;
    }

    public String getFile()
    {
        return file;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    public String getTarget()
    {
        return target;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    public String getArgs()
    {
        return args;
    }

    public void setArgs(String args)
    {
        this.args = args;
    }

    protected void setupContext(VelocityContext context)
    {
        if (TextUtils.stringSet(file))
        {
            context.put("jamfile", file);
        }

        if (TextUtils.stringSet(target))
        {
            context.put("targets", target);
        }

        if (TextUtils.stringSet(args))
        {
            context.put("arguments", args);
        }

        if (TextUtils.stringSet(work))
        {
            context.put("workingDir", work);
        }

    }

    protected String getTemplateName()
    {
        return "bjam.template.vm";
    }
}
