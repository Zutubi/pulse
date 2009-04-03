package com.zutubi.pulse.core.marshal.doc;

/**
 * Holds a single example of how to configure a type using XML.
 */
public class ExampleDocs
{
    private String name;
    private String blurb;
    private String xmlSnippet;

    public ExampleDocs(String name, String blurb, String xmlSnippet)
    {
        this.name = name;
        this.blurb = blurb;
        this.xmlSnippet = xmlSnippet;
    }

    public String getName()
    {
        return name;
    }

    public String getBlurb()
    {
        return blurb;
    }

    public String getXmlSnippet()
    {
        return xmlSnippet;
    }
}
