package com.zutubi.pulse.core.marshal.doc;

/**
 * Stores details of a child element as it nests within the context of some
 * parent element.  This includes the type-specific element docs as well as
 * the context-specific element name and arity.
 */
public class ChildElementDocs
{
    private String name;
    private ElementDocs elementDocs;
    private Arity arity;

    public ChildElementDocs(String name, ElementDocs elementDocs, Arity arity)
    {
        this.name = name;
        this.elementDocs = elementDocs;
        this.arity = arity;
    }

    public String getName()
    {
        return name;
    }

    public ElementDocs getElementDocs()
    {
        return elementDocs;
    }

    public Arity getArity()
    {
        return arity;
    }
}
