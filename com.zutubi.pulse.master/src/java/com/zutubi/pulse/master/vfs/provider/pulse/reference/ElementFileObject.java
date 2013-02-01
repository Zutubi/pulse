package com.zutubi.pulse.master.vfs.provider.pulse.reference;

import com.google.common.base.Function;
import com.zutubi.pulse.core.marshal.doc.*;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.util.CollectionUtils;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.Collection;

/**
 * A file object representing an {@link com.zutubi.pulse.core.marshal.doc.ElementDocs}
 * node in the tove file doc tree.
 */
public class ElementFileObject extends AbstractReferenceFileObject
{
    private ElementDocs elementDocs;

    public ElementFileObject(final FileName name, final AbstractFileSystem fs, ElementDocs elementDocs)
    {
        super(name, fs);
        this.elementDocs = elementDocs;
    }

    protected String[] getDynamicChildren()
    {
        Collection<ChildNodeDocs> children = elementDocs.getChildren();
        return CollectionUtils.mapToArray(children, new Function<ChildNodeDocs, String>()
        {
            public String apply(ChildNodeDocs childNodeDocs)
            {
                return childNodeDocs.getName();
            }
        }, new String[children.size()]);
    }

    public AbstractPulseFileObject createDynamicFile(FileName fileName)
    {
        NodeDocs nodeDocs = elementDocs.getNode(fileName.getBaseName());
        if (nodeDocs == null)
        {
            return null;
        }

        if (nodeDocs instanceof ElementDocs)
        {
            return objectFactory.buildBean(ElementFileObject.class, new Class[]{FileName.class, AbstractFileSystem.class, ElementDocs.class}, new Object[]{fileName, getFileSystem(), nodeDocs});
        }
        else if (nodeDocs instanceof ExtensibleDocs)
        {
            return objectFactory.buildBean(ExtensibleFileObject.class, new Class[]{FileName.class, AbstractFileSystem.class, ExtensibleDocs.class}, new Object[]{fileName, getFileSystem(), nodeDocs});
        }
        else
        {
            return objectFactory.buildBean(BuiltinElementFileObject.class, new Class[]{FileName.class, AbstractFileSystem.class, BuiltinElementDocs.class}, new Object[]{fileName, getFileSystem(), nodeDocs});
        }
    }

    @Override
    public String getIconCls()
    {
        return "reference-element-icon";
    }

    public ElementDocs getElementDocs()
    {
        return elementDocs;
    }
}
