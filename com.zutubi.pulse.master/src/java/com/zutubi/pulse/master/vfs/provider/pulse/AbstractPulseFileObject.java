package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.util.bean.ObjectFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The Pulse File Object is the base class for all the file objects handled by the Pulse File System.
 */
public abstract class AbstractPulseFileObject extends AbstractFileObject
{
    protected ObjectFactory objectFactory;

    protected BuildManager buildManager;

    protected ProjectManager projectManager;

    /**
     * A reference to the containing pulse file system instance.
     */
    protected PulseFileSystem pfs;

    /**
     * Constructor.
     * 
     * @param name  the name of this file object instance.
     * @param fs    the filesystem this file belongs to. 
     */
    public AbstractPulseFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
        this.pfs = (PulseFileSystem) fs;
    }

    /**
     * Every implementation of the pulse file object is responsible for the creation of its own children
     * via this factory method.
     * <p>
     * Note: this may be called when the file is not attached.
     *
     * @param fileName  is the name of the child file.
     *
     * @return the child file object instance.
     *
     * @throws Exception if there are any problems creating the child.
     */
    public abstract AbstractPulseFileObject createFile(final FileName fileName) throws Exception;

    /**
     * Return the human readable name of this file object. This name may differ from the file system name for
     * this file.  This name should not be used when traversing the filesystem.
     *
     * @return the name of this file.
     */
    public String getDisplayName()
    {
        return getName().getBaseName();
    }

    /**
     * Retrieve a list of the actions that are supported by this file object.
     *
     * @return a list of action names.
     */
    public List<FileAction> getActions()
    {
        return Collections.emptyList();
    }

    /**
     * Returns an extra classification for the object, which may be used for
     * categorising or styling in the UI.
     *
     * @return an extra class name, or null for no extra class
     */
    public String getCls()
    {
        return null;
    }

    /**
     * Returns an icon classification for the object, which may be used for
     * categorising or styling in the UI.
     *
     * @return an extra class name, or null for no extra class
     */
    public String getIconCls()
    {
        return null;
    }

    /**
     * Returns a java.io.File instance if this FileObject has a file system equivalent.
     *
     * @return returns a file, or null.
     */
    public File toFile()
    {
        return null;
    }

    /**
     * This method searches through the file object hierarchy looking for, and returning when found, a file object
     * that matches the specified type.
     *
     * @param type  the type of the ancestor being searched for.
     *
     * @return the matching ancestor, or null if none match.
     *
     * @throws FileSystemException if there are any problems that prevent searching the hierarchy.
     */
    public <T> T getAncestor(Class<T> type) throws FileSystemException
    {
        for(AbstractPulseFileObject ancestor = this; ancestor != null; ancestor = (AbstractPulseFileObject) ancestor.getParent())
        {
            if (type.isInstance(ancestor))
            {
                return type.cast(ancestor);
            }
        }

        return null;
    }

    /**
     * Simple noop implementation of the required interface method. Override this method if required.
     *
     * @return 0
     *
     * @throws Exception
     */
    protected long doGetContentSize() throws Exception
    {
        return 0;
    }

    /**
     * Simple noop implementation of the required interface method. Override this method if required.
     *
     * @return null
     *
     * @throws Exception
     */
    protected InputStream doGetInputStream() throws Exception
    {
        return null;
    }

    public String getFileType() throws FileSystemException
    {
        FileType type = getType();
        if (type == FileType.FOLDER)
        {
            return FileTypeConstants.FOLDER;
        }
        if (type == FileType.FILE)
        {
            return FileTypeConstants.FILE;
        }
        return FileTypeConstants.UNKNOWN;
    }

    /**
     * Returns an arbitrary set of extra name-value pairs for this file object.
     * Allows custom fields to be added for specific types of files.  By
     * default, null is returned (i.e. no attributes).  Override to customise
     * this in a subclass.
     *
     * @return a mapping of name-value pairs containing extra attributes, may
     *         be null if there are no such attributes
     */
    public Map<String, String> getExtraAttributes()
    {
        return null;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    /**
     * Required resource. Enough of the subclasses use the build manager for it to be
     * generally useful.
     *
     * @param buildManager instance.
     */
    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
