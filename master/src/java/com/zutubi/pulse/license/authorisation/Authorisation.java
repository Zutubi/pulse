package com.zutubi.pulse.license.authorisation;

import com.zutubi.pulse.license.License;

/**
 * <class-comment/>
 */
public interface Authorisation
{
    String[] getAuthorisation(License license);
}
