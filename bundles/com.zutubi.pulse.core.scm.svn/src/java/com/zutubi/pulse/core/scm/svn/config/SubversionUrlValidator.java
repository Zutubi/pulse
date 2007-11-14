package com.zutubi.pulse.core.scm.svn.config;

import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorSupport;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

/**
 * Validates that a string will parse as a Subversion URL.
 */
public class SubversionUrlValidator extends FieldValidatorSupport
{
    public void validate(Object obj) throws ValidationException
    {
        Object value = getFieldValue(getFieldName(), obj);
        if (value != null && value instanceof String)
        {
            String url = (String) value;

            if (url.length() > 0)
            {
                try
                {
                    SVNURL.parseURIDecoded(url);
                }
                catch (SVNException e)
                {
                    setDefaultMessage(e.getMessage());
                    addFieldError(getFieldName());
                }
            }
        }
    }
}
