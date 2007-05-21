package com.zutubi.pulse.prototype.config.types;

import com.zutubi.config.annotations.SymbolicName;
import com.opensymphony.util.TextUtils;
import org.apache.velocity.VelocityContext;

/**
 *
 *
 */
@SymbolicName("internal.antTypeConfig")
public class AntTypeConfiguration extends TemplateTypeConfiguration
{
    private String work;
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
            context.put("buildFile", file);
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

//        context.put("environment", environment);

    }

    protected String getTemplateName()
    {
        return "ant.template.vm";
    }
}
