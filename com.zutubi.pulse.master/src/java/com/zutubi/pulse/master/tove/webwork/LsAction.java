package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.master.vfs.provider.pulse.ComparatorProvider;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.vfs.DirectoryComparator;
import com.zutubi.pulse.master.xwork.actions.vfs.FileObjectWrapper;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.TextUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.UriParser;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Action for listing config objects.
 */
public class LsAction extends ActionSupport
{
    private String basePath;
    private String path;
    private FileSystemManager fileSystemManager;
    private ExtFile[] listing;

    public String getBasePath()
    {
        return basePath;
    }

    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public ExtFile[] getListing()
    {
        return listing;
    }

    public String execute() throws Exception
    {
        String fullPath = "pulse:///";
        if(TextUtils.stringSet(basePath))
        {
            fullPath += "/" + UriParser.encode(PathUtils.normalisePath(basePath));
        }
        if(TextUtils.stringSet(path))
        {
            fullPath += "/" + UriParser.encode(PathUtils.normalisePath(path));
        }

        final FileObject fileObject = fileSystemManager.resolveFile(fullPath);

        // can only list a file object if
        // a) it is a directory
        if (fileObject.getType() != FileType.FOLDER)
        {
            return ERROR;
        }

        // b) the user has read permissions.
        if (!fileObject.isReadable())
        {
            addActionError("You do not have permission to list this folder.");
            return ERROR;
        }

        try
        {
            FileObject[] children = fileObject.getChildren();
            if (children != null)
            {
                sortChildren(fileObject, children);
                listing = new ExtFile[children.length];
                CollectionUtils.mapToArray(children, new Mapping<FileObject, ExtFile>()
                {
                    public ExtFile map(FileObject child)
                    {
                        return new ExtFile(new FileObjectWrapper(child, fileObject));
                    }
                }, listing);
            }
        }
        catch (FileSystemException e)
        {
// FIXME
            e.printStackTrace();
        }

        return SUCCESS;
    }

    private void sortChildren(FileObject fileObject, FileObject[] children)
    {
        Comparator<FileObject> comparator = getComparator(fileObject);
        if (comparator != null)
        {
            Arrays.sort(children, comparator);
        }
    }

    private Comparator<FileObject> getComparator(FileObject parentFile)
    {
        if(parentFile instanceof AbstractPulseFileObject)
        {
            try
            {
                ComparatorProvider provider = ((AbstractPulseFileObject) parentFile).getAncestor(ComparatorProvider.class);
                if (provider != null)
                {
                    return provider.getComparator();
                }
            }
            catch (FileSystemException e)
            {
                // Fall through to default.
            }
        }

        return new DirectoryComparator();
    }

    public void setFileSystemManager(FileSystemManager fileSystemManager)
    {
        this.fileSystemManager = fileSystemManager;
    }

}
