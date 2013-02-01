package com.zutubi.pulse.master.vfs.provider.pulse.file;

import com.google.common.base.Function;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.servercore.filesystem.FileInfo;
import com.zutubi.util.CollectionUtils;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.util.List;

/**
 * A base implementation for any file object that intends to be the root of a
 * file info subtree.
 */
public abstract class FileInfoRootFileObject extends AbstractPulseFileObject implements FileInfoProvider
{
    private static final String ROOT_PATH = "";

    public FileInfoRootFileObject(FileName name, AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(FileName fileName) throws Exception
    {
        FileInfo child = getFileInfo(fileName.getBaseName());

        return objectFactory.buildBean(FileInfoFileObject.class,
                new Class[]{FileInfo.class, FileName.class, AbstractFileSystem.class},
                new Object[]{child, fileName, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        List<FileInfo> children = getFileInfos(ROOT_PATH);
        if (children == null)
        {
            return NO_CHILDREN;
        }
        
        return UriParser.encode(CollectionUtils.mapToArray(children, new Function<FileInfo, String>()
        {
            public String apply(FileInfo fileInfo)
            {
                return fileInfo.getName();
            }
        }, new String[children.size()]));
    }
}
