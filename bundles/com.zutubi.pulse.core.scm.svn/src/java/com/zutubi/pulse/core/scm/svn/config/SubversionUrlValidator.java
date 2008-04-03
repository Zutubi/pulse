package com.zutubi.pulse.core.scm.svn.config;

import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.StringFieldValidatorSupport;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

/**
 * Validates that a string will parse as a Subversion URL.
 */
public class SubversionUrlValidator extends StringFieldValidatorSupport
{
    public void validateStringField(String url) throws ValidationException
    {
        try
        {
            SVNURL.parseURIDecoded(url);
        }
        catch (SVNException e)
        {
            addErrorMessage(e.getMessage());
        }
    }
}
