package com.zutubi.pulse.core.marshal.doc;

import java.util.*;

/**
 * Documentation for an element in a tove file, including all child attributes
 * and elements that may nest within it.  The element name is stored
 * externally, as the same element type may appear in multiple contexts under
 * different names.
 */
public class ElementDocs extends NodeDocs
{
    private ContentDocs contentDocs;
    private SortedMap<String, AttributeDocs> attributes = new TreeMap<String, AttributeDocs>();
    private SortedMap<String, ChildNodeDocs> children = new TreeMap<String, ChildNodeDocs>();
    private List<ExampleDocs> examples = new LinkedList<ExampleDocs>();

    public ElementDocs(String brief, String verbose)
    {
        super(brief, verbose);
    }

    public ContentDocs getContentDocs()
    {
        return contentDocs;
    }

    public void setContentDocs(ContentDocs contentDocs)
    {
        this.contentDocs = contentDocs;
    }

    public Collection<AttributeDocs> getAttributes()
    {
        return Collections.unmodifiableCollection(attributes.values());
    }

    public AttributeDocs getAttribute(String name)
    {
        return attributes.get(name);
    }

    public void addAttribute(AttributeDocs attributeDocs)
    {
        attributes.put(attributeDocs.getName(), attributeDocs);
    }

    public Collection<ChildNodeDocs> getChildren()
    {
        return Collections.unmodifiableCollection(children.values());
    }

    public ChildNodeDocs getChild(String name)
    {
        return children.get(name);
    }

    public void addChild(ChildNodeDocs childNodeDocs)
    {
        children.put(childNodeDocs.getName(), childNodeDocs);
    }

    public List<ExampleDocs> getExamples()
    {
        return examples;
    }

    public void addExample(ExampleDocs exampleDocs)
    {
        examples.add(exampleDocs);
    }

    @Override
    public NodeDocs getNode(final String name)
    {
        ChildNodeDocs childNodeDocs = getChild(name);
        return childNodeDocs == null ? null : childNodeDocs.getNodeDocs();
    }

    @Override
    public boolean isElement()
    {
        return true;
    }
}
