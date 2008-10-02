package com.zutubi.pulse.web.project;

import com.opensymphony.xwork.ValidationAware;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.master.tove.config.project.types.CustomProjectValidationPredicate;
import com.zutubi.util.StringUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.io.ByteArrayInputStream;

/**
 */
public class CustomDetailsHelper
{
    private int lineNumber;
    private String line;
    private int lineOffset;
    private PulseFileLoaderFactory fileLoaderFactory;

    public CustomDetailsHelper(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }

    public int getLineNumber()
    {
        return lineNumber;
    }

    public String getLine()
    {
        return line;
    }

    public int getLineOffset()
    {
        return lineOffset;
    }

    public void validate(ValidationAware action, String pulseFile, ResourceRepository resourceRepository)
    {
        if(!TextUtils.stringSet(pulseFile))
        {
            action.addFieldError("details.pulseFile", "pulse file is required");
            return;
        }

        try
        {
            PulseFileLoader loader = fileLoaderFactory.createLoader();
            loader.setObjectFactory(new DefaultObjectFactory());
            
            loader.load(new ByteArrayInputStream(pulseFile.getBytes()), new PulseFile(), new PulseScope(), resourceRepository, new CustomProjectValidationPredicate());
        }
        catch(ParseException pe)
        {
            action.addActionError(pe.getMessage());
            if(pe.getLine() > 0)
            {
                line = StringUtils.getLine(pulseFile, pe.getLine());
                if(line != null)
                {
                    lineNumber = pe.getLine();
                    lineOffset = StringUtils.getLineOffset(pulseFile, lineNumber);
                    action.addActionError("First line of offending element: " + line);
                }
            }
        }
        catch(Exception e)
        {
            action.addActionError(e.getMessage());
        }
    }
}
