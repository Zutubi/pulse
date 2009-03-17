package com.zutubi.pulse.core.marshal.doc;

/**
 * Represents a built-in element, such as &lt;macro&gt;.  The bulk of the
 * documentation for these elements is defined statically.
 */
public class BuiltinElementDocs extends NodeDocs
{
    public BuiltinElementDocs(String brief, String verbose)
    {
        super(brief, verbose);
    }

    public NodeDocs getNode(String name)
    {
        return null;
    }

    public boolean isElement()
    {
        return true;
    }
}
