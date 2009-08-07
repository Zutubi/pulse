package com.zutubi.pulse.master.xwork.actions.vfs;

import com.zutubi.pulse.master.vfs.CompoundFileFilter;
import com.zutubi.pulse.master.vfs.FilePrefixFilter;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.master.vfs.provider.pulse.ComparatorProvider;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.*;

import java.util.*;

/**
 * The ls action provides access to 'ls' style functionality for the web ui.
 * 
 */
public class LsAction extends VFSActionSupport
{
    private static final Logger LOG = Logger.getLogger(LsAction.class);

    private boolean showFiles = false;
    private boolean showHidden = false;
    private String prefix = null;
    private int depth = 1;

    /**
     * @deprecated 
     */
    private String root;

    private String path;

    private List<FileObjectWrapper> listing = null;

    /**
     * @deprecated just use the path instead.
     */
    public void setRoot(String root)
    {
        this.root = root;
    }

    /**
     * Show files indicates whether or not the listing should include files. The default value is false.
     *
     * @param showFiles     if true, files will be included in the listing.
     */
    public void setShowFiles(boolean showFiles)
    {
        this.showFiles = showFiles;
    }

    /**
     * Show files that are marked as hidden. The default value for this is false.
     *
     * @param showHidden    if true, hidden files will be included in the listing.
     */
    public void setShowHidden(boolean showHidden)
    {
        this.showHidden = showHidden;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    /**
     * Specify the base path that will be listed. This path should define a directory. It does not make sense to
     * list a file since, by definition, it has no children.
     *
     * @param path
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * The depth defines how many child directories will be traversed and listed. By default, the depth is 1.
     *
     * For example, a depth of 1 will result in the base path being listed. A depth of 2 will list the base path
     * and all of the directories in that listing.
     *
     * This is useful for expanding multiple levels of the directory tree at one time.
     *
     * @param depth
     */
    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    public List<FileObjectWrapper> getListing()
    {
        return listing;
    }

    public String execute() throws Exception
    {
        try
        {
            doList();
            return SUCCESS;
        }
        catch (FileSystemException e)
        {
            LOG.error(e);
            addActionError(e.getMessage());
            return ERROR;
        }
    }

    public void doList() throws FileSystemException
    {
        // provide temporary backwards compatibility for the deprecated root variable. 
        if (StringUtils.stringSet(root))
        {
            path = root + path;
        }
        
        FileObject fo = getFS().resolveFile(path);

        // can only list a file object if
        // a) it is a directory
        if (fo.getType() != FileType.FOLDER)
        {
            return;
        }

        // b) the user has read permissions.
        if (!fo.isReadable())
        {
            addActionError("You do not have permission to list this folder.");
            return;
        }

        Collection<FileType> acceptedTypes = new HashSet<FileType>();
        acceptedTypes.add(FileType.FOLDER);
        if (showFiles)
        {
            acceptedTypes.add(FileType.FILE);
        }

        listing = new LinkedList<FileObjectWrapper>();
        FileObject[] children = fo.findFiles(new FileDepthFilterSelector(new CompoundFileFilter(FileTypeFilter.accept(acceptedTypes), new FilePrefixFilter(prefix)), depth));

        if (children != null)
        {
            sortChildren(fo, children);
            for (FileObject child : children)
            {
                if (showHidden || !child.isHidden())
                {
                    listing.add(new FileObjectWrapper(child, fo));
                }
            }
        }
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
