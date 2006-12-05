package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.List;
import java.util.LinkedList;
import java.io.File;

import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.ProjectManager;

/**
 * The Pulse File Object is the base class for all the file objects handled by the Pulse File System.
 *
 *
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
    public List<String> getActions()
    {
        return new LinkedList<String>();
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
     * Override this method if you need a post creation callback in which the wiried depenedencies are
     * available.
     *  
     */
    public void init()
    {
        
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
    public AbstractPulseFileObject getAncestor(Class type) throws FileSystemException
    {
        if (type.isAssignableFrom(this.getClass()))
        {
            return this;
        }
        AbstractPulseFileObject parent = (AbstractPulseFileObject) getParent();
        if (parent != null)
        {
            return parent.getAncestor(type);
        }
        return null;
    }

    /**
     * Required resource.
     *
     * @param objectFactory instance.
     */
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

    /**
     * Required resource.
     *
     * @param projectManager instance
     */
    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
