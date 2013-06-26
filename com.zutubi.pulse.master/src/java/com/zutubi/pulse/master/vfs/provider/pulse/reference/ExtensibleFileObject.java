package com.zutubi.pulse.master.vfs.provider.pulse.reference;

import com.zutubi.pulse.core.marshal.doc.ElementDocs;
import com.zutubi.pulse.core.marshal.doc.ExtensibleDocs;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.util.Sort;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A file object representing an {@link com.zutubi.pulse.core.marshal.doc.ExtensibleDocs}
 * node in the tove file doc tree.
 */
public class ExtensibleFileObject extends AbstractReferenceFileObject
{
    private ExtensibleDocs extensibleDocs;

    public ExtensibleFileObject(final FileName name, final AbstractFileSystem fs, ExtensibleDocs extensibleDocs)
    {
        super(name, fs);
        this.extensibleDocs = extensibleDocs;
    }

    protected String[] getDynamicChildren()
    {
        List<String> children = new LinkedList<String>(extensibleDocs.getExtensions().keySet());
        Collections.sort(children, new Sort.StringComparator());
        return children.toArray(new String[children.size()]);
    }

    public AbstractPulseFileObject createDynamicFile(FileName fileName)
    {
        ElementDocs child = extensibleDocs.getExtensions().get(fileName.getBaseName());
        if (child == null)
        {
            return null;
        }

        return objectFactory.buildBean(ElementFileObject.class, fileName, getFileSystem(), child);
    }

    @Override
    public String getIconCls()
    {
        return "reference-extensible-icon";
    }

    public ExtensibleDocs getExtensibleDocs()
    {
        return extensibleDocs;
    }
}