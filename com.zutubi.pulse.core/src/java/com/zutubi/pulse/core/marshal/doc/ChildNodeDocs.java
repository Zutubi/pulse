package com.zutubi.pulse.core.marshal.doc;

/**
 * Stores details of a child node as it nests within the context of some
 * parent element.  This includes the type-specific node docs as well as
 * the context-specific name and arity.
 */
public class ChildNodeDocs
{
    private String name;
    private NodeDocs nodeDocs;
    private Arity arity;

    public ChildNodeDocs(String name, NodeDocs nodeDocs, Arity arity)
    {
        this.name = name;
        this.nodeDocs = nodeDocs;
        this.arity = arity;
    }

    public String getName()
    {
        return name;
    }

    public NodeDocs getNodeDocs()
    {
        return nodeDocs;
    }

    public Arity getArity()
    {
        return arity;
    }
}
