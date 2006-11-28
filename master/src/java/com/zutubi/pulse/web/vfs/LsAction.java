package com.zutubi.pulse.web.vfs;

import org.apache.commons.vfs.*;

import java.util.*;

/**
 * <class comment/>
 */
public class LsAction extends VFSActionSupport
{
    private boolean showFiles = false;

    private boolean showHidden = false;

    private String root;

    private String path;

    private List listing = null;

    public void setRoot(String root)
    {
        this.root = root;
    }

    public void setShowFiles(boolean showFiles)
    {
        this.showFiles = showFiles;
    }

    public void setShowHidden(boolean showHidden)
    {
        this.showHidden = showHidden;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public List getListing()
    {
        return listing;
    }

    public String execute() throws Exception
    {
        doList();
        return SUCCESS;
    }

    public void doList() throws FileSystemException
    {
        FileObject fo = getFS().resolveFile(root + path);

        // can only list a file object if
        // a) it is a directory
        if (fo.getType() != FileType.FOLDER)
        {
            return;
        }

        // b) the user has read permissions.
        if (!fo.isReadable())
        {
            return;
        }

        Collection<FileType> acceptedTypes = new HashSet<FileType>();
        acceptedTypes.add(FileType.FOLDER);
        if (showFiles)
        {
            acceptedTypes.add(FileType.FILE);
        }

        listing = new LinkedList<FileObjectWrapper>();
        FileObject[] children = fo.findFiles(new FileFilterSelector(FileTypeFilter.accept(acceptedTypes)));

        if (children != null)
        {
            Collections.sort(Arrays.asList(children), new DirectoryComparator());
        }

        if (children != null)
        {
            for (FileObject child : children)
            {
                if (showHidden || !child.isHidden())
                {
                    listing.add(new FileObjectWrapper(child));
                }
            }
        }
    }

    private static class FileTypeFilter implements FileFilter
    {
        private Collection<FileType> acceptedTypes = new HashSet<FileType>();

        public boolean accept(final FileSelectInfo fileInfo)
        {
            try
            {
                return acceptedTypes.contains(fileInfo.getFile().getType());
            }
            catch (FileSystemException e)
            {
                e.printStackTrace();
                return false;
            }
        }

        public static FileFilter accept(Collection<FileType> types)
        {
            FileTypeFilter filter = new FileTypeFilter();
            filter.acceptedTypes.addAll(types);
            return filter;
        }
    }
}
