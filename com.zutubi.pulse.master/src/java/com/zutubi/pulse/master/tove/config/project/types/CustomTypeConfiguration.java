package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.TextArea;
import com.zutubi.config.annotations.Transient;
import com.zutubi.config.annotations.Wire;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.personal.PatchArchive;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.StringUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

import java.io.ByteArrayInputStream;
import java.util.List;

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

    public String getPulseFile(ProjectConfiguration projectConfig, Revision revision, PatchArchive patch)
    {
        return pulseFileString;
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
        if(!TextUtils.stringSet(pulseFileString))
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

            loader.load(new ByteArrayInputStream(pulseFileString.getBytes()), new PulseFile(), new PulseScope(), new EmptyResourceRepository(), new CustomProjectValidationPredicate());
        }
        catch(ParseException pe)
        {
            context.addActionError(pe.getMessage());
            if(pe.getLine() > 0)
            {
                String line = StringUtils.getLine(pulseFileString, pe.getLine());
                if(line != null)
                {
//                    int lineNumber = pe.getLine();
//                    int lineOffset = StringUtils.getLineOffset(pulseFileString, lineNumber);
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

    private static class EmptyResourceRepository implements ResourceRepository
    {
        public boolean hasResource(ResourceRequirement requirement)
        {
            return false;
        }

        public boolean hasResource(String name)
        {
            return false;
        }

        public Resource getResource(String name)
        {
            return null;
        }

        public List<String> getResourceNames()
        {
            return null;
        }
    }
}
