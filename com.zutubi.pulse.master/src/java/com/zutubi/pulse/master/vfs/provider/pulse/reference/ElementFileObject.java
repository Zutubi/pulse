package com.zutubi.pulse.master.vfs.provider.pulse.reference;

import com.zutubi.pulse.core.marshal.doc.*;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.Collection;

/**
 * A file object representing an {@link com.zutubi.pulse.core.marshal.doc.ElementDocs}
 * node in the tove file doc tree.
 */
public class ElementFileObject extends AbstractPulseFileObject
{
    private ElementDocs elementDocs;

    public ElementFileObject(final FileName name, final AbstractFileSystem fs, ElementDocs elementDocs)
    {
        super(name, fs);
        this.elementDocs = elementDocs;
    }

    public AbstractPulseFileObject createFile(FileName fileName) throws Exception
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

    protected FileType doGetType() throws Exception
    {
        return elementDocs.getChildren().size() == 0 ? FileType.FILE : FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        Collection<ChildNodeDocs> children = elementDocs.getChildren();
        return CollectionUtils.mapToArray(children, new Mapping<ChildNodeDocs, String>()
        {
            public String map(ChildNodeDocs childNodeDocs)
            {
                return childNodeDocs.getName();
            }
        }, new String[children.size()]);
    }

    public ElementDocs getElementDocs()
    {
        return elementDocs;
    }
}
