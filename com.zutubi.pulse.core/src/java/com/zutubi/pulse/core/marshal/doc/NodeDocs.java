package com.zutubi.pulse.core.marshal.doc;

/**
 * Abstract base for nodes in the documentation tree.  Holds common fields.
 */
public abstract class NodeDocs
{
    private String brief;
    private String verbose;

    public NodeDocs(String brief, String verbose)
    {
        this.brief = brief;
        this.verbose = verbose;
    }

    public String getBrief()
    {
        return brief;
    }

    public String getVerbose()
    {
        return verbose;
    }

    public abstract NodeDocs getNode(String name);
    public abstract boolean isElement();
}
