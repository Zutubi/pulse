package com.zutubi.pulse.master.vfs.provider.pulse.reference;

import com.zutubi.pulse.core.marshal.doc.BuiltinElementDocs;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * A file object representing a {@link com.zutubi.pulse.core.marshal.doc.BuiltinElementDocs}
 * node in the tove file doc tree.
 */
public class BuiltinElementFileObject extends AbstractPulseFileObject
{
    private BuiltinElementDocs elementDocs;

    public BuiltinElementFileObject(final FileName name, final AbstractFileSystem fs, BuiltinElementDocs elementDocs)
    {
        super(name, fs);
        this.elementDocs = elementDocs;
    }

    public AbstractPulseFileObject createFile(FileName fileName) throws Exception
    {
        return null;
    }

    @Override
    public String getIconCls()
    {
        return "reference-builtin-icon";
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FILE;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[0];
    }

    public BuiltinElementDocs getElementDocs()
    {
        return elementDocs;
    }
}