package com.zutubi.pulse.license.authorisation;

import com.zutubi.pulse.license.License;

/**
 * <class-comment/>
 */
public interface Authorisation
{
    String[] NO_AUTH = new String[0];

    String[] getAuthorisation(License license);
}
