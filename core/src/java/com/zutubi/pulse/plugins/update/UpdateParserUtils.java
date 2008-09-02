package com.zutubi.pulse.plugins.update;

import com.zutubi.pulse.util.XMLUtils;
import com.zutubi.pulse.plugins.PluginVersion;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import java.net.URI;
import java.net.URL;

/**
 */
public class UpdateParserUtils
{
    static final String ELEMENT_DESCRIPTION = "description";
    static final String ATTRIBUTE_URL = "url";
    static final String ATTRIBUTE_VERSION = "version";

    static String getOptionalText(Element element, String name)
    {
        String text = null;
        Elements children = element.getChildElements(name);
        if(children.size() > 0)
        {
            text = XMLUtils.getText(children.get(0));
        }
        return text;
    }

    static URL resolveURL(String urlString, URL url) throws ParsingException
    {
        try
        {
            if (url == null)
            {
                return new URL(urlString);   
            }
            else
            {
                URI uri = new URI(urlString);
                if (uri.isAbsolute())
                {
                    url = uri.toURL();
                }
                else
                {
                    url = new URL(url, urlString);
                }
            }
        }
        catch (Exception e)
        {
            throw new ParsingException("Invalid url '" + urlString + "': " + e.getMessage(), e);
        }

        return url;
    }

    static URL getRequiredURL(Site site, Element e) throws ParsingException
    {
        String urlString = XMLUtils.getRequiredAttribute(e, ATTRIBUTE_URL);
        return resolveURL(urlString, site == null ? null : site.getURL());
    }

    static PluginVersion getVersion(Element e, boolean required) throws ParsingException
    {
        String versionString;
        if(required)
        {
            versionString = XMLUtils.getRequiredAttribute(e, ATTRIBUTE_VERSION);
        }
        else
        {
            versionString = e.getAttributeValue(ATTRIBUTE_VERSION);
            if(versionString == null)
            {
                return null;
            }
        }        

        try
        {
            return new PluginVersion(versionString);
        }
        catch(IllegalArgumentException ex)
        {
            throw new ParsingException("Invalid version: " + ex.getMessage());
        }
    }
}
