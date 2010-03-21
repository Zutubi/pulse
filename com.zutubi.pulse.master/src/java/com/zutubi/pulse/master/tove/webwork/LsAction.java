package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.master.vfs.provider.pulse.ComparatorProvider;
import com.zutubi.pulse.master.vfs.CompoundFileFilter;
import com.zutubi.pulse.master.vfs.FilePrefixFilter;
import com.zutubi.pulse.master.xwork.actions.vfs.DirectoryComparator;
import com.zutubi.pulse.master.xwork.actions.vfs.FileObjectWrapper;
import com.zutubi.pulse.master.xwork.actions.vfs.VFSActionSupport;
import com.zutubi.pulse.master.xwork.actions.vfs.FileDepthFilterSelector;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.StringUtils;
import org.apache.commons.vfs.*;
import org.apache.commons.vfs.provider.UriParser;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Collection;
import java.util.HashSet;

/**
 * The ls action provides access to 'ls' style functionality for the web ui.
 */
public class LsAction extends VFSActionSupport
{
    private String fs ="pulse";
    private String prefix;

    /**
     * The base path for the request.  This, combined with the {@link #path}
     * define the path to be listed.
     */
    private String basePath;

    /**
     * The path, relative to the base path, that defines what should be listed.
     */
    private String path;

    /**
     * The results of the ls action.
     */
    private ExtFile[] listing;

    /**
     * Show files indicates whether or not the listing should include files. The default value is false.
     */
    private boolean showFiles = true;

    /**
     * Show files that are marked as hidden. The default value for this is false.
     */
    private boolean showHidden;

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

    public void setFs(String fs)
    {
        this.fs = fs;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public void setShowFiles(boolean showFiles)
    {
        this.showFiles = showFiles;
    }

    public void setShowHidden(boolean showHidden)
    {
        this.showHidden = showHidden;
    }

    public ExtFile[] getListing()
    {
        return listing;
    }

    public String execute() throws Exception
    {
        String fullPath = fs + "://";
        if(StringUtils.stringSet(basePath))
        {
            fullPath += "/" + UriParser.encode(PathUtils.normalisePath(basePath));
        }
        if(StringUtils.stringSet(path))
        {
            fullPath += "/" + UriParser.encode(PathUtils.normalisePath(path));
        }

        final FileObject fileObject = getFS().resolveFile(fullPath);

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

        Collection<FileType> acceptedTypes = new HashSet<FileType>();
        acceptedTypes.add(FileType.FOLDER);
        if (showFiles)
        {
            acceptedTypes.add(FileType.FILE);
        }

        FileObject[] children = fileObject.findFiles(
                new FileDepthFilterSelector(
                        new CompoundFileFilter(
                                new FileTypeFilter(acceptedTypes),
                                new HiddenFileFilter(showHidden),
                                new FilePrefixFilter(prefix)
                        ), 1
                )
        );
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

    /**
     * Filter that accepts only specified types of files.
     */
    private static class FileTypeFilter implements FileFilter
    {
        private Collection<FileType> acceptedTypes = new HashSet<FileType>();

        private FileTypeFilter(Collection<FileType> acceptedTypes)
        {
            this.acceptedTypes = acceptedTypes;
        }

        public boolean accept(final FileSelectInfo fileInfo)
        {
            try
            {
                return acceptedTypes.contains(fileInfo.getFile().getType());
            }
            catch (FileSystemException e)
            {
                return false;
            }
        }
    }

    /**
     * Filter based on the files hidden flag.
     */
    private static class HiddenFileFilter implements FileFilter
    {
        private boolean showHidden;

        private HiddenFileFilter(boolean showHidden)
        {
            this.showHidden = showHidden;
        }

        public boolean accept(FileSelectInfo fileSelectInfo)
        {
            try
            {
                FileObject file = fileSelectInfo.getFile();
                return showHidden || !file.isHidden();
            }
            catch (FileSystemException e)
            {
                return false;
            }
        }
    }
}
