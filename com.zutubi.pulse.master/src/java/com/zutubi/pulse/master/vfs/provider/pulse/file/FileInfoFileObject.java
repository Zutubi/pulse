package com.zutubi.pulse.master.vfs.provider.pulse.file;

import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.servercore.filesystem.FileInfo;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.util.List;

/**
 * File object implementation based on top of FileInfo and FileInfoProvider
 */
public class FileInfoFileObject extends AbstractPulseFileObject
{
    private FileInfo fileInfo;

    public FileInfoFileObject(FileInfo fileInfo, FileName name, AbstractFileSystem fs)
    {
        super(name, fs);
        this.fileInfo = fileInfo;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        FileInfoProvider provider = getAncestor(FileInfoProvider.class);
        String relativePath = ((FileObject)provider).getName().getRelativeName(fileName);

        FileInfo child = provider.getFileInfo(relativePath);

        return objectFactory.buildBean(FileInfoFileObject.class,
                new Class[]{FileInfo.class, FileName.class, AbstractFileSystem.class},
                new Object[]{child, fileName, pfs}
        );
    }

    protected String[] doListChildren() throws Exception
    {
        FileInfoProvider provider = getAncestor(FileInfoProvider.class);
        if (provider != null)
        {
            FileObject root = (FileObject) provider;
            String relativePath = root.getName().getRelativeName(getName());

            List<FileInfo> children = provider.getFileInfos(relativePath);
            return UriParser.encode(CollectionUtils.mapToArray(children, new Mapping<FileInfo, String>()
            {
                public String map(FileInfo fileInfo)
                {
                    return fileInfo.getName();
                }
            }, new String[children.size()]));
        }
        return NO_CHILDREN;
    }

    protected FileType doGetType() throws Exception
    {
        if (fileInfo.isDirectory())
        {
            return FileType.FOLDER;
        }
        else if (fileInfo.isFile())
        {
            return FileType.FILE;
        }
        return FileType.IMAGINARY;
    }

    protected long doGetContentSize() throws Exception
    {
        return fileInfo.length();
    }
}
