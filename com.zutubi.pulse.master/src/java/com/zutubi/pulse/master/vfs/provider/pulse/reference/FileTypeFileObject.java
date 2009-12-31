package com.zutubi.pulse.master.vfs.provider.pulse.reference;

import com.zutubi.pulse.core.marshal.doc.ElementDocs;
import com.zutubi.pulse.core.marshal.doc.ToveFileDocManager;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Represents a type of tove file (e.g. a pulse file) in the tove file doc
 * tree.
 */
public class FileTypeFileObject extends AbstractReferenceFileObject
{
    private String root;
    private String displayName;
    private ToveFileDocManager toveFileDocManager;

    public FileTypeFileObject(final FileName name, final AbstractFileSystem fs, String displayName)
    {
        super(name, fs);
        this.root = name.getBaseName();
        this.displayName = displayName;
    }

    protected String[] getDynamicChildren()
    {
        return new String[]{root};
    }

    public AbstractPulseFileObject createDynamicFile(FileName fileName)
    {
        ElementDocs child = toveFileDocManager.lookupRoot(fileName.getBaseName());
        if (child == null)
        {
            return null;
        }

        return objectFactory.buildBean(ElementFileObject.class, new Class[]{FileName.class, AbstractFileSystem.class, ElementDocs.class}, new Object[]{fileName, getFileSystem(), child});
    }

    @Override
    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public String getIconCls()
    {
        return "reference-filetype-icon";
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    public void setToveFileDocManager(ToveFileDocManager toveFileDocManager)
    {
        this.toveFileDocManager = toveFileDocManager;
    }
}
