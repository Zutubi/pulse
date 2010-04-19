package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.PulseScope;
import com.zutubi.pulse.core.engine.FixedPulseFileProvider;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.PulseFileProvider;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoader;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.marshal.ImportingNotSupportedFileResolver;
import com.zutubi.pulse.core.marshal.ParseException;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.TextArea;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

import java.io.ByteArrayInputStream;

/**
 * A pulse file project where the pulse file is edited directly in Pulse
 * itself.
 */
@SymbolicName("zutubi.customTypeConfig")
@Wire
public class CustomTypeConfiguration extends TypeConfiguration implements Validateable
{
    @TextArea(rows = 30, cols = 80)
    private String pulseFileString;
    @Transient
    private PulseFileLoaderFactory fileLoaderFactory;

    public PulseFileProvider getPulseFile()
    {
        return new FixedPulseFileProvider(pulseFileString);
    }

    public String getPulseFileString()
    {
        return pulseFileString;
    }

    public void setPulseFileString(String pulseFileString)
    {
        this.pulseFileString = pulseFileString;
    }

    public void validate(ValidationContext context)
    {
        if(!StringUtils.stringSet(pulseFileString))
        {
            context.addFieldError("pulseFileString", "pulse file is required");
            return;
        }

        if(fileLoaderFactory == null)
        {
            // This happens when we are validated during startup.  Although
            // it feels a bit sloppy, actually the only time it is really
            // important to do the next validation is when the user is making
            // changes.
            return;
        }

        try
        {
            PulseFileLoader loader = fileLoaderFactory.createLoader();
            loader.setObjectFactory(new DefaultObjectFactory());

            loader.load(new ByteArrayInputStream(pulseFileString.getBytes()), new ProjectRecipesConfiguration(), new PulseScope(), new ImportingNotSupportedFileResolver(), new CustomProjectValidationInterceptor());
        }
        catch(ParseException pe)
        {
            context.addActionError(pe.getMessage());
            if(pe.getLine() > 0)
            {
                String line = StringUtils.getLine(pulseFileString, pe.getLine());
                if(line != null)
                {
                    context.addActionError("First line of offending element: " + line);
                }
            }
        }
        catch(Exception e)
        {
            context.addActionError(e.getMessage());
        }
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }
}
