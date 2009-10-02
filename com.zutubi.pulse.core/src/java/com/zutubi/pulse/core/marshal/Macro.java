package com.zutubi.pulse.core.marshal;

import com.zutubi.tove.variables.api.Variable;
import nu.xom.Element;

/**
 * A macro in a pulse file, which associates a name with a fragment of XML.
 * This allows the same fragment to be referenced using macro-ref an
 * arbitrary number of times in the file.
 */
public class Macro implements Variable
{
    private String name;
    private Element element;

    public Macro(String name, Element element)
    {
        this.name = name;
        this.element = element;
    }

    public String getName()
    {
        return name;
    }

    public Element getValue()
    {
        return element;
    }
}
