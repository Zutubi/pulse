package com.zutubi.pulse.core.marshal.doc;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Documentation for an element in a tove file, including all child attributes
 * and elements that may nest within it.  The element name is stored
 * externally, as the same element type may appear in multiple contexts under
 * different names.
 */
public class ElementDocs
{
    private String brief;
    private String verbose;
    private ContentDocs contentDocs;
    private List<AttributeDocs> attributes = new LinkedList<AttributeDocs>();
    private List<ChildElementDocs> children = new LinkedList<ChildElementDocs>();

    public ElementDocs(String brief, String verbose)
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

    public ContentDocs getContentDocs()
    {
        return contentDocs;
    }

    public void setContent(ContentDocs contentDocs)
    {
        this.contentDocs = contentDocs;
    }

    public List<AttributeDocs> getAttributes()
    {
        return Collections.unmodifiableList(attributes);
    }

    public AttributeDocs getAttribute(final String name)
    {
        return CollectionUtils.find(attributes, new Predicate<AttributeDocs>()
        {
            public boolean satisfied(AttributeDocs attributeDocs)
            {
                return attributeDocs.getName().equals(name);
            }
        });
    }

    public void addAttribute(AttributeDocs attributeDocs)
    {
        attributes.add(attributeDocs);
    }

    public List<ChildElementDocs> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    public ChildElementDocs getChild(final String name)
    {
        return CollectionUtils.find(children, new Predicate<ChildElementDocs>()
        {
            public boolean satisfied(ChildElementDocs childElementDocs)
            {
                return childElementDocs.getName().equals(name);
            }
        });
    }

    public void addChild(ChildElementDocs childElementDocs)
    {
        children.add(childElementDocs);
    }
}
