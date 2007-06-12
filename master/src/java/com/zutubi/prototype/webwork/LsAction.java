package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.web.vfs.DirectoryComparator;
import com.zutubi.pulse.web.vfs.FileObjectWrapper;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;

import java.util.Arrays;

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
            fullPath += "/" + PathUtils.normalizePath(basePath);
        }
        if(TextUtils.stringSet(path))
        {
            fullPath += "/" + PathUtils.normalizePath(path);
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

        FileObject[] children = fileObject.getChildren();
        if (children != null)
        {
            Arrays.sort(children, new DirectoryComparator());
            listing = new ExtFile[children.length];
            CollectionUtils.mapToArray(children, new Mapping<FileObject, ExtFile>()
            {
                public ExtFile map(FileObject child)
                {
                    return new ExtFile(new FileObjectWrapper(child, fileObject));
                }
            }, listing);
        }

        return SUCCESS;
    }

    public void setFileSystemManager(FileSystemManager fileSystemManager)
    {
        this.fileSystemManager = fileSystemManager;
    }

    public static class ExtFile
    {
        private String id;
        private String text;
        private boolean leaf;

        public ExtFile(FileObjectWrapper fo)
        {
            id = fo.getId();
            text = fo.getId();
            leaf = !fo.isContainer();
        }

        public String getId()
        {
            return id;
        }

        public String getText()
        {
            return text;
        }

        public boolean isLeaf()
        {
            return leaf;
        }
    }

}
