package com.zutubi.pulse.plugins.update;

import java.net.URL;

/**
 * Reference from a feature to an update site where related features may be
 * found.
 */
public class DiscoverySiteReference
{
    /**
     * Location of the site.
     */
    private URL url;
    /**
     * Human-readable name for the site.
     */
    private String label;
    /**
     * If true, the site is not an Eclipse update site, but rather a plain
     * website.
     */
    private boolean web = true;


    public DiscoverySiteReference(URL url, String label, boolean web)
    {
        this.url = url;
        this.label = label;
        this.web = web;
    }
}
