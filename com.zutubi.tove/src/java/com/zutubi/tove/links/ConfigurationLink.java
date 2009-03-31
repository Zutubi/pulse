package com.zutubi.tove.links;

/**
 * Represents a single link from a configuration page to a related page.
 */
public class ConfigurationLink
{
    private String name;
    private String url;
    private String label;

    /**
     * Creates a new link with the given name and label.  The name should be
     * unique for a configuration type, and is used to help look up the label.
     *
     * @param name name of the link
     * @param url  the url to link to, relative to the base url
     */
    public ConfigurationLink(String name, String url)
    {
        this.name = name;
        this.url = stripLeadingSlash(url);
    }

    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return url;
    }

    public String getLabel()
    {
        return label;
    }

    /**
     * Sets the label to show to the user.  If not set, the label will be
     * determined using normal i18n lookup for the key &lt;name&gt;.label.
     *
     * @param label label to display as the link text
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    private String stripLeadingSlash(String s)
    {
        if (s.startsWith("/"))
        {
            return s.substring(1);
        }
        else
        {
            return s;
        }
    }
}
