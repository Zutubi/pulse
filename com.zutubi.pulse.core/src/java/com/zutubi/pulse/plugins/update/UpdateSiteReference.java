package com.zutubi.pulse.plugins.update;

import java.net.URL;

/**
 * A reference from a feature to an update site where updates for that
 * feature may be found.
 */
public class UpdateSiteReference
{
    /**
     * Location of the site.
     */
    private URL url;
    /**
     * Human-readable name of the site.
     */
    private String label;


    public UpdateSiteReference(URL url, String label)
    {
        this.url = url;
        this.label = label;
    }
}
