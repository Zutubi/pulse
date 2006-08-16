package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ValidationAware;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.model.CustomProjectValidationPredicate;
import com.zutubi.pulse.util.StringUtils;

import java.io.ByteArrayInputStream;

/**
 */
public class CustomDetailsHelper
{
    public static void validate(ValidationAware action, String pulseFile, ResourceRepository resourceRepository)
    {
        if(!TextUtils.stringSet(pulseFile))
        {
            action.addFieldError("details.pulseFile", "pulse file is required");
            return;
        }

        try
        {
            PulseFileLoader loader = new PulseFileLoader(new ObjectFactory());
            loader.load(new ByteArrayInputStream(pulseFile.getBytes()), new PulseFile(), new Scope(), resourceRepository, new CustomProjectValidationPredicate());
        }
        catch(ParseException pe)
        {
            action.addActionError(pe.getMessage());
            if(pe.getLine() > 0)
            {
                String line = StringUtils.getLine(pulseFile, pe.getLine());
                if(line != null)
                {
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
