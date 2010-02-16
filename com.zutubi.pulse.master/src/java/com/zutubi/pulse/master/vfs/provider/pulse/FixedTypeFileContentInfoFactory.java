package com.zutubi.pulse.master.vfs.provider.pulse;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileContentInfo;
import org.apache.commons.vfs.FileContentInfoFactory;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileContentInfo;

/**
 * A trivial content info factory that allows the file content type to be
 * specified.
 */
class FixedTypeFileContentInfoFactory implements FileContentInfoFactory
{
    private String type;

    /**
     * Creates a new content info factory.
     *
     * @param type content type of the files this factory creates info for
     */
    public FixedTypeFileContentInfoFactory(String type)
    {
        this.type = type;
    }

    public FileContentInfo create(FileContent fileContent) throws FileSystemException
    {
        return new DefaultFileContentInfo(type, null);
    }
}
