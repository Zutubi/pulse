package com.zutubi.tove.links;

/**
 * Represents a single link from a configuration page to a related page.
 */
public class ConfigurationLink
{
    private static final String DEFAULT_ICON = "link_go.gif";

    private String name;
    private String url;
    private String label;
    private String icon = DEFAULT_ICON;

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

    /**
     * Creates a new link with the given name, label and custom icon image.
     * The name should be unique for a configuration type, and is used to
     * help look up the label.
     *
     * @param name name of the link
     * @param url  the url to link to, relative to the base url
     * @param icon icon file to use, as a path relative to the images base
     */
    public ConfigurationLink(String name, String url, String icon)
    {
        this(name, url);
        this.icon = stripLeadingSlash(icon);
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

    public String getIcon()
    {
        return icon;
    }

    /**
     * Sets the path to a custom icon image file, relative to the base images
     * directory.  If not set, a generic link image is used.
     *
     * @param icon icon file to use, as a path relative to the images base
     */
    public void setIcon(String icon)
    {
        this.icon = stripLeadingSlash(icon);
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
