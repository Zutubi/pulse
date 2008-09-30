package com.zutubi.pulse.plugins.update;

import java.net.URL;

/**
 * Represents an update site remembered by Pulse as a place to look for
 * features to install.
 */
public class UpdateSite
{
    private URL url;
    private String label;

    public UpdateSite(URL url, String label)
    {
        this.url = url;
        this.label = label;
    }

    public URL getUrl()
    {
        return url;
    }

    public String getLabel()
    {
        return label;
    }
}
