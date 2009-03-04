package com.zutubi.pulse.core.marshal.doc;

/**
 * A description of how text content nested within an element is used.  For
 * most elements, there is no such documentation as nested text is not
 * supported, but in some cases the text is bound to a simple property or
 * used in lieu of an attribute for addable simple lists.
 */
public class ContentDocs
{
    private String verbose;

    public ContentDocs(String verbose)
    {
        this.verbose = verbose;
    }

    public String getVerbose()
    {
        return verbose;
    }
}
