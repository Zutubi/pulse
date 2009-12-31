package com.zutubi.pulse.master.vfs.provider.pulse.reference;

import com.zutubi.pulse.core.marshal.doc.BuiltinElementDocs;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * A file object representing a {@link com.zutubi.pulse.core.marshal.doc.BuiltinElementDocs}
 * node in the tove file doc tree.
 */
public class BuiltinElementFileObject extends AbstractReferenceFileObject
{
    private BuiltinElementDocs elementDocs;

    public BuiltinElementFileObject(final FileName name, final AbstractFileSystem fs, BuiltinElementDocs elementDocs)
    {
        super(name, fs);
        this.elementDocs = elementDocs;
    }

    protected String[] getDynamicChildren()
    {
        return new String[0];
    }

    public AbstractPulseFileObject createDynamicFile(FileName fileName)
    {
        return null;
    }

    @Override
    public String getIconCls()
    {
        return "reference-builtin-icon";
    }

    public BuiltinElementDocs getElementDocs()
    {
        return elementDocs;
    }
}